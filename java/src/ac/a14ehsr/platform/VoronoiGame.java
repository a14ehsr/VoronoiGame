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
import java.util.ArrayList;
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
        numberOfPlayers = setting.getNumberOfPlayers();
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

        int patternSize = 1;
        for (int i=1; i<=numberOfPlayers; i++) {
            patternSize*=i;
        }

        for (int p = 0; p < numberOfPlayers; p++) {
            outputStreams[p].write((numberOfPlayers + "\n").getBytes());
            outputStreams[p].write((numberOfGames + "\n").getBytes());
            outputStreams[p].write((numberOfSelectNodes + "\n").getBytes());
            outputStreams[p].write((patternSize + "\n").getBytes());
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
        int[][][] gameRecord = new int[numberOfGames][][];


        // プレイヤーの手番の管理用リスト．線形リストで十分．
        List<int[]> sequenceList = new ArrayList<>();
        sequenceList = Permutation.of(numberOfPlayers);
        // numberOfGames回対戦
        for (int i = 0; i < numberOfGames; i++) {
            graph = new GridGraph(10, 10);
            gameRecord[i] = new int[graph.getNumberOfNodes()][2];
            for (int[] pair : gameRecord[i]) {
                Arrays.fill(pair, -1);
            }
            for (int p = 0; p < numberOfPlayers; p++) {
                outputStreams[p].write((graph.toString()).getBytes()); // graph情報
                outputStreams[p].flush();
                
            }
            for (int[] sequence : sequenceList) {
                for (int p = 0; p < numberOfPlayers; p++) {
                    for (int num : sequence) {
                        outputStreams[p].write((num + "\n").getBytes()); // graph情報
                        outputStreams[p].flush();
                    }
                }
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
                /*
                for (int a=0; a<10; a++) {
                    for (int b=0; b<10; b++) {
                        System.out.printf("%2d ",gameRecord[i][a*10+b][0]);
                    }
                    System.out.println();
                }
                System.out.println();
                */
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
     * 対戦の実行 TODO: 任意のプレイヤー数に対応するために，組み合わせを再帰を使って書き直す
     */
    /*
    private void autoRun() {
        List<String> commandList = setting.getCommandList();
        String[] names = new String[commandList.size()];
        int[][] resultTable = new int[names.length][names.length];
        for (int i = 0; i < names.length; i++) {
            for (int j = i + 1; j < names.length; j++) {
                try {
                    startSubProcess(new String[] { commandList.get(i), commandList.get(j) });
                    Result result = run();
                    names[i] = result.names[0];
                    names[j] = result.names[1];
                    resultTable[i][j] = result.playerPoints[i];
                    resultTable[j][i] = result.playerPoints[j];
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    processDestroy();
                }
            }
        }
        result(names, resultTable);
    }
    */
    private void autoRun() {
        List<String> commandList = setting.getCommandList();
        String[] names = new String[commandList.size()];
        List<Result> resultList = new ArrayList<>();
        int[] matching = new int[numberOfPlayers];
        autoRun(commandList, names, resultList, matching, 0);
        for (String name : names) {
            System.out.print(name + " ");
        }
        System.out.println();
        resultList.forEach(System.out::println);
    }

    private void autoRun(List<String> commandList, String[] names, List<Result> resultList,int[] matching, int count) {
        if (numberOfPlayers == count) {
            for (int num : matching) {
                System.out.print(num + " ");
            }
            System.out.println();
            // 対戦とリザルトの格納
            String[] commands = new String[numberOfPlayers];
            for (int i = 0; i < numberOfPlayers; i++) {
                commands[i] = commandList.get(matching[i]);
            }
            System.out.println("comand");
            for (String com : commands) {
                System.out.print(" " + com);
            }
            try {
                startSubProcess(commands);
                Result result = run();
                String[] resultNames = result.names;
                for (int i = 0; i < numberOfPlayers; i++) {
                    names[matching[i]] = resultNames[i];
                }
                resultList.add(result);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                processDestroy();
            }

            return;
        }
        if (count == 0) {
            for (int i = 0; i < commandList.size(); i++) {
                matching[0] = i;
                autoRun(commandList, names, resultList, matching, count + 1);
            }
        }

        // matching[count]番目以降との組み合わせだけを考える
        for (int i = matching[count-1]+1; i < commandList.size(); i++) {
            matching[count] = i;
            autoRun(commandList, names, resultList, matching, count+1);
        }
    } 

    /**
     * リザルトの出力
     */
    private void result(String[] names, int[][] score) {
        System.out.println("RESULT");
        for (int i = 0; i < names.length; i++) {
            System.out.printf("%18s ", names[i]);
            for (int j = 0; j < names.length; j++) {
                System.out.printf("%3d ", score[i][j]);
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
        for (int i = 0; i < names.length; i++) {
            pw.printf(",%s", names[i]);
        }
        pw.println();
        for (int i = 0; i < names.length; i++) {
            pw.printf("%s,", names[i]);
            for (int j = 0; j < names.length; j++) {
                pw.printf("%d,", score[i][j]);
            }
            pw.println();
        }
        pw.close();

    }

    /**
     * テスト実行によるふるい
     */
    private void test() {
        List<String> commandList = setting.getCommandList();
        List<String> sampleCommandList = setting.getSampleCommandList();
        Logger testRunLogger = Logger.getLogger(VoronoiGame.class.getName());
        loggerInit(testRunLogger, "resource/log/test_run_err/err.log");

        // 実行コマンド出力ファイルの準備
        FileWriter file = null;
        try {
            file = new FileWriter("resource/command_list/command_list_green.txt");
        } catch (Exception e) {
            e.printStackTrace();
        }
        PrintWriter pw = new PrintWriter(new BufferedWriter(file));

        // サンプルと対戦させ，例外が発生しなかれば，実行可能コマンドとしてファイルに出力
        for (int i = 0; i < commandList.size(); i++) {
            String playerCommand = commandList.get(i);
            System.out.println(playerCommand);
            try {
                for (String command : sampleCommandList) {
                    startSubProcess(new String[] { playerCommand, command });
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
