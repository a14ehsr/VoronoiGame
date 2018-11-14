//
//  VoronoiGame.java
//
//  Created by Hirano Keisuke on 2018/11/08.
//  Copyright © 2018年 Hirano Keisuke. All rights reserved.
//
package ac.a14ehsr.platform;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import ac.a14ehsr.platform.graph.Graph;
import ac.a14ehsr.platform.graph.GridGraph;

public class VoronoiGame {

    Process[] processes;
    InputStream[] inputStreams;
    OutputStream[] outputStreams;
    BufferedReader[] bufferedReaders;
    Setting setting;
    int numberOfGames;
    int numberOfSelectNodes;
    int outputLevel;
    String[] outputStr;
    int numberOfPlayers;
    Graph graph;

    int timeout = 1000;

    public VoronoiGame(String[] args) {
        // 各種設定と実行コマンド関連の処理
        setting = new Setting();
        setting.start(args);
        // パラメータ取得
        numberOfGames = setting.getNumberOfGames();
        numberOfSelectNodes = setting.getNumberOfSelectNodes();
        outputLevel = setting.getOutputLevel();

    }

    /**
     * サブプロセスの起動
     * 
     * @param cmd 実行コマンド(0:攻撃，1:防御)
     * @throws IOException
     */
    private void startSubProcess(String[] cmd) throws IOException {
        Runtime rt = Runtime.getRuntime();
        processes = new Process[numberOfPlayers];
        inputStreams = new InputStream[numberOfPlayers];
        outputStreams = new OutputStream[numberOfPlayers];
        bufferedReaders = new BufferedReader[numberOfPlayers];

        for (int i = 0; i < numberOfPlayers; i++) {
            processes[i] = rt.exec(cmd[i]);
            outputStreams[i] = processes[i].getOutputStream();
            inputStreams[i] = processes[i].getInputStream();
            bufferedReaders[i] = new BufferedReader(new InputStreamReader(inputStreams[i]));
            new ErrorReader(processes[i].getErrorStream()).start();
            if (!processes[i].isAlive())
                throw new IOException("次のサブプロセスを起動できませんでした．:" + processes[i]);
        }
    }

    private void getOutput(int index) throws IOException {
        outputStr[index] = bufferedReaders[index].readLine();
    }

    /**
     * 対戦する
     * 
     * @throws IOException
     * @throws AgainstTheRulesException
     * @throws NumberFormatException
     */
    private Result run() throws IOException, AgainstTheRulesException, NumberFormatException, TimeoutException {
        try {
            Thread.sleep(100);
        } catch (Exception e) {
            e.printStackTrace();
        }

        String[] names = new String[numberOfPlayers];
        for (int p = 0; p < numberOfPlayers; p++) {
            outputStreams[p].write((numberOfNode + "\n").getBytes());
            outputStreams[p].write((numberOfGame + "\n").getBytes());
            outputStreams[p].write((numberOfSelectNode + "\n").getBytes());
            outputStreams[p].write((p + "\n").getBytes()); // player code
            outputStreams[p].flush();
            names[p] = bufferedReaders[p].readLine();
        }

        if (outputLevel > 0) {
            System.out.print("players : ");
            for (String name : names)
                System.out.printf(name + " ");
            System.out.println();
        }

        outputStr = new String[numberOfPlayers];
        // 利得のレコード
        int[][] gainRecord = new int[numberOfGames][numberOfPlayers];

        // 各プレイヤーの勝利数
        int[] playerPoints = new int[numberOfPlayers];

        // ゲームレコードの準備(初期値-1)
        int[][][] gameRecord = new int[numberOfGames][numberOfNodes][2];
        for (int[][] record : gameRecord) {
            for (int[] pair : record) {
                Arrays.fill(pair, -1);
            }
        }

        // プレイヤーの手番の管理用リスト．線形リストで十分．
        List<int[]> sequenceList = new ArrayList<>();
        for (int i = 0; i < numberOfPlayers; i++) {
            sequence.add(i);
        }
        sequenceList = Permutation.of(numberOfPlayers);
        // numberOfGames回対戦
        for (int i = 0; i < numberOfGames; i++) {
            graph = new GridGraph(10, 10);
            for (int p = 0; p < numberOfPlayers; p++) {
                outputStreams[p].write((graph).getBytes()); // graph情報
                outputStreams[p].flush();
            }
            for (int[] sequence : sequenceList) {
                // 選択するノード数分のループ
                for (int j = 0; j < numberOfSelectNodes; j++) {
                    // 各プレイヤーのループ
                    for (int p : sequence) {
                        // それぞれの数字を取得
                        Thread thread = new GetResponseThread(p);
                        thread.start();
                        try {
                            thread.join(timeout);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                        if (!processes[p].isAlive())
                            throw new IOException("次のプレイヤーのサブプロセスが停止しました :" + names[p]);
                        if (outputStr[p] == null)
                            throw new TimeoutException("一定時間以内に次のプレイヤーから値を取得できませんでした :" + names[p]);

                        int num;
                        try {
                            num = Integer.parseInt(outputStr[p]);
                        } catch (NumberFormatException e) {
                            throw new NumberFormatException("次のプレイヤーから整数以外の値を取得しました :" + names[p]);
                        }
                        gain(p, num, gameRecord[i], names[p]);
                        gameRecord[i][num][1] = j;
                        for (int pp : sequence) {
                            if (pp == p)
                                continue;
                            outputStreams[pp].write((num + "\n").getBytes());
                            outputStreams[pp].flush();
                        }

                    }
                }
                // 勝ち点の計算
                evaluate(gameRecord[i], gainRecord[i], playerPoints);
            }

        }
        if (outputLevel > 0) {
            // TODO:resultの出力
        }
        return new Result(names, playerPoints);
    }

    /**
     * 点数の計算を行う recordからgainを取得・記録し， 勝ち点をplayerPointsに加算する．
     * 
     * @param record       ゲームの記録
     * @param gain         プレイヤーごとの獲得利得
     * @param playerPoints プレイヤーごとの勝ち点
     */
    private void evaluate(final int[][] record, int[] gain, int[] playerPoints) {

    }

    /**
     * サブプロセスを終了
     */
    private void processDestroy() {
        for (Process p : processes) {
            if (p == null)
                continue;
            try {
                p.destroy();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * プレイヤーによるノードの獲得を制御
     * 
     * @param player 獲得プレイヤー
     * @param node   獲得ノード
     * @param record レコード
     * @param names  プレイヤーネーム
     * @throws AgainstTheRulesException ルール違反例外
     */
    private void gain(int player, int node, int[][] record, String name) throws AgainstTheRulesException {
        if (record[node][0] != -1) {
            throw new AgainstTheRulesException("次のプレイヤーが既に獲得されたノードを選択しました：" + name);
        }
        record[node][0] = player;
    }

    public static void main(String[] args) {
        VoronoiGame obj = new VoronoiGame(args);
        if (obj.setting.isTest()) {
            obj.test();
        } else {
            obj.autoRun();
        }

    }

    /**
     * 対戦の実行
     */
    private void autoRun() {
        List<String> attackCommandList = setting.getAttackCommand();
        List<String> defenceCommandList = setting.getDefenceCommand();
        String[] attackNames = new String[attackCommandList.size()];
        String[] defenceNames = new String[defenceCommandList.size()];
        double[][] resultTable = new double[attackNames.length][defenceNames.length];
        for (int i = 0; i < attackNames.length; i++) {
            for (int j = 0; j < defenceNames.length; j++) {
                try {
                    startSubProcess(new String[] { attackCommandList.get(i), defenceCommandList.get(j) });
                    Result result = run();
                    attackNames[i] = result.names[0];
                    defenceNames[j] = result.names[1];
                    resultTable[i][j] = ((double) result.hitRoundSum / result.hit);
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    processDestroy();
                }
            }
        }
        result(attackNames, defenceNames, resultTable);
    }

    /**
     * リザルトの出力
     */
    private void result(String[] attackNames, String[] defenceNames, double[][] score) {
        System.out.println("RESULT");
        for (int i = 0; i < attackNames.length; i++) {
            System.out.printf("%18s ", attackNames[i]);
            for (int j = 0; j < defenceNames.length; j++) {
                System.out.printf("%6.3f ", score[i][j]);
            }
            System.out.println();
        }

        // リザルト出力用ファイルの準備
        FileWriter file = null;
        try {
            file = new FileWriter("resource/result/result.csv");
        } catch (Exception e) {
            e.printStackTrace();
        }
        PrintWriter pw = new PrintWriter(new BufferedWriter(file));
        pw.println("RESULT");
        for (int i = 0; i < defenceNames.length; i++) {
            pw.printf(",%s", defenceNames[i]);
        }
        pw.println();
        for (int i = 0; i < attackNames.length; i++) {
            pw.printf("%s,", attackNames[i]);
            for (int j = 0; j < defenceNames.length; j++) {
                pw.printf("%f,", score[i][j]);
            }
            pw.println();
        }
        pw.close();

    }

    /**
     * テスト実行によるふるい
     */
    private void test() {
        List<String> attackCommandList = setting.getAttackCommand();
        List<String> defenceCommandList = setting.getDefenceCommand();
        List<String> sampleAttackCommandList = setting.getSampleAttackCommand();
        List<String> sampleDefenceCommandList = setting.getSampleDefenceCommand();
        Logger testRunLogger = Logger.getLogger(VoronoiGame.class.getName());
        loggerInit(testRunLogger, "resource/log/test_run_err/err.log");

        // 実行コマンド出力ファイルの準備
        FileWriter file = null;
        try {
            file = new FileWriter("resource/command_list/attack/attack_command_list_green.txt");
        } catch (Exception e) {
            e.printStackTrace();
        }
        PrintWriter pw = new PrintWriter(new BufferedWriter(file));

        // サンプルと対戦させ，例外が発生しなかれば，実行可能コマンドとしてファイルに出力
        // @TODO タイムアウト処理
        for (int i = 0; i < attackCommandList.size(); i++) {
            String playerCommand = attackCommandList.get(i);
            System.out.println(playerCommand);
            try {
                for (int j = 0; j < sampleDefenceCommandList.size(); j++) {
                    startSubProcess(new String[] { playerCommand, sampleDefenceCommandList.get(j) });
                    run();
                    processDestroy();
                }
                pw.println(playerCommand);
            } catch (AgainstTheRulesException e) {
                testRunLogger.log(Level.INFO, "テスト実行時エラー :", e);
            } catch (NumberFormatException e) {
                testRunLogger.log(Level.INFO, "テスト実行時エラー :", e);
            } catch (IOException e) {
                System.err.println(e);
            } catch (TimeoutException e) {
                System.err.println(e);
            } finally {
                processDestroy();
            }
        }
        pw.close();

        // 実行コマンド出力ファイルの準備
        try {
            file = new FileWriter("resource/command_list/defence/defence_command_list_green.txt");
        } catch (Exception e) {
            e.printStackTrace();
        }
        pw = new PrintWriter(new BufferedWriter(file));

        // サンプルと対戦させ，例外が発生しなかれば，実行可能コマンドとしてファイルに出力
        // @TODO タイムアウト処理
        for (int i = 0; i < defenceCommandList.size(); i++) {
            String playerCommand = defenceCommandList.get(i);
            System.out.println(playerCommand);
            try {
                for (int j = 0; j < sampleAttackCommandList.size(); j++) {
                    startSubProcess(new String[] { sampleAttackCommandList.get(j), playerCommand });
                    run();
                    processDestroy();
                }
                pw.println(playerCommand);
            } catch (AgainstTheRulesException e) {
                testRunLogger.log(Level.INFO, "テスト実行時エラー :", e);
            } catch (NumberFormatException e) {
                testRunLogger.log(Level.INFO, "テスト実行時エラー :", e);
            } catch (IOException e) {
                System.err.println(e);
            } catch (TimeoutException e) {
                System.err.println(e);
            } finally {
                processDestroy();
            }
        }
        pw.close();
    }

    /**
     * Loggerの初期化等
     * 
     * @param logger   初期化対象オブジェクト
     * @param filePath ログ出力ファイルのパス
     */
    private void loggerInit(Logger logger, String filePath) {
        FileHandler fileHandler = null;
        try {
            fileHandler = new FileHandler(filePath, false);
        } catch (IOException e) {
            e.printStackTrace();
        }

        fileHandler.setFormatter(new SimpleFormatter());
        logger.addHandler(fileHandler);
        logger.setLevel(Level.INFO);
        // Loggerクラスのメソッドを使ってメッセージを出力
        logger.finest("FINEST");
        logger.finer("FINER");
        logger.fine("FINE");
        logger.config("CONFIG");
        logger.info("INFO");
        logger.warning("WARNING");
        logger.severe("SEVERE");
    }

    /**
     * ルール違反発生時用の例外クラス
     */
    class AgainstTheRulesException extends Exception {
        /**
         * コンストラクタ
         * 
         * @param mes メッセージ
         */
        AgainstTheRulesException(String mes) {
            super(mes);
        }
    }

    /**
     * タイムアウト発生時に投げる例外クラス
     */
    class TimeoutException extends Exception {
        /**
         * コンストラクタ
         * 
         * @param mes メッセージ
         */
        TimeoutException(String mes) {
            super(mes);
        }
    }

    /**
     * 数値の取得用Thread
     */
    class GetResponseThread extends Thread {
        private int index;

        GetResponseThread(int index) {
            this.index = index;
        }

        public void run() {
            try {
                getOutput(index);
            } catch (IOException e) {
                e.printStackTrace();
                // outputException = e;
            }
        }
    }

    /**
     * リザルト用のEntity
     */
    class Result {
        String[] names;
        int[] playerPoints;

        Result(String[] names, int[] playerPoints) {
            this.names = names;
            this.playerPoints = playerPoints;
        }
    }
}

/**
 * エラー出力のReader
 */
class ErrorReader extends Thread {
    InputStream error;

    public ErrorReader(InputStream is) {
        error = is;
    }

    public void run() {
        try {
            byte[] ch = new byte[50000];
            int read;
            while ((read = error.read(ch)) > 0) {
                String s = new String(ch, 0, read);
                System.out.print(s);
                System.out.flush();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
