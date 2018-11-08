//
//  GraphEncampmentGame.java
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
import java.util.List;
import java.util.Scanner;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class GraphEncampmentGame {

    Process[] processes;
    InputStream[] inputStreams;
    OutputStream[] outputStreams;
    BufferedReader[] bufferedReaders;
    Setting setting;
    int min, max, change;
    String[] outputStr;

    int timeout = 5000;

    public GraphEncampmentGame(String[] args) {
        // 各種設定と実行コマンド関連の処理
        setting = new Setting();
        setting.start(args);

        min = setting.getMin();
        max = setting.getMax();
        change = setting.getChange();

    }

    /**
     * サブプロセスの起動
     * 
     * @param cmd 実行コマンド(0:攻撃，1:防御)
     * @throws IOException
     */
    private void startSubProcess(String[] cmd) throws IOException {
        Runtime rt = Runtime.getRuntime();
        int numberOfPlayers = cmd.length;
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
        // パラメータ取得
        int n = setting.getGameNum();
        int round = setting.getRoundNum();
        int outputLevel = setting.getOutputLevel();

        String[] names = new String[2];
        for (int p = 0; p < 2; p++) {
            outputStreams[p].write((min + "\n").getBytes());
            outputStreams[p].write((max + "\n").getBytes());
            outputStreams[p].write((n + "\n").getBytes());
            outputStreams[p].write((round + "\n").getBytes());
            outputStreams[p].write((change + "\n").getBytes());
            outputStreams[p].flush();
            names[p] = bufferedReaders[p].readLine();
        }

        if (outputLevel > 0)
            System.out.println("player1 : " + names[0] + " vs " + names[1] + " : player2");

        int[][][] gameRecord = new int[n][round][2];
        int hit = 0;
        int hitRoundSum = 0;

        // n回対戦
        for (int i = 0; i < n; i++) {
            int count = 10;
            int beforeDeffnceNumber = -1;
            // round回のラウンド
            int j = 0;
            for (; j < round; j++) {
                // それぞれの数字を取得
                Thread[] threads = new Thread[2];
                outputStr = new String[2];
                for (int p = 0; p < 2; p++) {
                    threads[p] = new GetResponseThread(p);
                    threads[p].start();
                    try {
                        threads[p].join(timeout);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                for (int p = 0; p < 2; p++) {
                    if (!processes[p].isAlive())
                        throw new IOException("次のプレイヤーのサブプロセスが停止しました :" + names[p]);
                    if (outputStr[p] == null)
                        throw new TimeoutException("一定時間以内に次のプレイヤーから値を取得できませんでした :" + names[p]);
                }
                int[] num = new int[2];
                for (int p = 0; p < 2; p++) {
                    try {
                        num[p] = Integer.parseInt(outputStr[p]);
                    } catch (NumberFormatException e) {
                        throw new NumberFormatException("次のプレイヤーから整数以外の値を取得しました :" + names[p]);
                    }
                }

                if (outputLevel > 2)
                    System.out.printf("   round%03d : (Attack,Defence) = (%3d,%3d)\n", j, num[0], num[1]);

                // 範囲内の数字かチェック
                checkRange(num, names);

                // 防御側の変更制約チェック @TODO テスト
                if (beforeDeffnceNumber == -1) {
                    beforeDeffnceNumber = num[1];
                } else if (beforeDeffnceNumber != num[1]) {
                    if (count < 9) {
                        throw new AgainstTheRulesException("規定回数以内にDefence側の数値が変更されました．");
                    } else {
                        count = 1;
                        beforeDeffnceNumber = num[1];
                    }
                } else {
                    count++;
                }
                // System.err.println("!"+count+"");

                // 攻撃側が防御側より上ならば，もっと下にするべきという意味で，ud=-1
                // 攻撃側が防御側より下ならば，もっと上にするべきという意味で，ud=1
                // 攻撃側と防御側が一致したならば，ぴったり一致という意味で，ud=0
                // とする．
                int ud;
                if (num[0] == num[1]) {
                    ud = 0;
                } else {
                    ud = num[0] < num[1] ? 1 : -1;
                }

                for (int p = 0; p < 2; p++) {
                    if (!processes[p].isAlive())
                        throw new IOException("次のプレイヤーのサブプロセスが停止しました :" + names[p]);
                }
                outputStreams[0].write((ud + "\n").getBytes());
                outputStreams[0].flush();
                outputStreams[1].write((num[0] + "\n").getBytes());
                outputStreams[1].flush();

                if (ud == 0) {
                    hit++;
                    hitRoundSum += (j + 1);
                    break;
                }

                // レコードへ記録
                for (int p = 0; p < 2; p++)
                    gameRecord[i][j][p] = num[p];

            }

            if (outputLevel > 1) {
                if (j < round)
                    System.out.printf("%3d回目でhit\n", (j + 1));
                else
                    System.out.println("hitならず");
            }

        }
        if (outputLevel > 0) {
            System.out.printf("hit回数/ゲーム数 = %3d/%d  |  hitのaverage = %f\n", hit, n, ((double) hitRoundSum / hit));
        }
        return new Result(names, hit, hitRoundSum);
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
     * 数字が範囲内かどうか
     * 
     * @param remain 残額
     * @param names  プレイヤー名
     * @throws AgainstTheRulesException ルール違反例外
     */
    private void checkRange(int[] number, String[] names) throws AgainstTheRulesException {
        for (int i = 0; i < 2; i++) {
            if (number[i] < min || number[i] > max)
                throw new AgainstTheRulesException(names[i] + "が範囲外の値を宣言しました．");
        }
    }

    public static void main(String[] args) {
        GraphEncampmentGame obj = new GraphEncampmentGame(args);
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
        Logger testRunLogger = Logger.getLogger(GraphEncampmentGame.class.getName());
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
        int hit;
        int hitRoundSum;

        Result(String[] names, int hit, int hitRoundSum) {
            this.names = names;
            this.hit = hit;
            this.hitRoundSum = hitRoundSum;
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
