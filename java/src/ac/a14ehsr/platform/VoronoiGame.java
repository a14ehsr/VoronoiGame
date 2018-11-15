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
import ac.a14ehsr.platform.gui.GraphDrawing;

public class VoronoiGame {

    Process[] processes;
    InputStream[] inputStreams;
    OutputStream[] outputStreams;
    BufferedReader[] bufferedReaders;
    Setting setting;
    int numberOfGames;
    int numberOfSelectNodes;
    int outputLevel;
    boolean isVisible;
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
        isVisible = setting.isVisible();
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
        for (int i = 1; i <= numberOfPlayers; i++) {
            patternSize *= i;
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
            System.out.print("players  : ");
            for (String name : names)
                System.out.printf(name + " ");
            System.out.println();
        }

        outputStr = new String[numberOfPlayers];
        // 利得のレコード
        int[][][] gainRecord = new int[numberOfGames][patternSize][numberOfPlayers];

        // 各プレイヤーの勝利数
        int[] playerPoints = new int[numberOfPlayers];

        // ゲームレコードの準備(初期値-1)
        int[][][][] gameRecord = new int[numberOfGames][][][];

        // プレイヤーの手番の管理用リスト．線形リストで十分．
        List<int[]> sequenceList = new ArrayList<>();
        sequenceList = Permutation.of(numberOfPlayers);
        // numberOfGames回対戦
        for (int i = 0; i < numberOfGames; i++) {
            graph = new GridGraph(10, 10);
            if (outputLevel >= 3) {
                graph.printWeight();
            }
            gameRecord[i] = new int[sequenceList.size()][graph.getNumberOfNodes()][2];
            for (int[][] sequenceRecord : gameRecord[i]) {
                for (int[] nodeInfo : sequenceRecord) {
                    Arrays.fill(nodeInfo, -1);
                }
            }

            for (int p = 0; p < numberOfPlayers; p++) {
                outputStreams[p].write((graph.toString()).getBytes()); // graph情報
                outputStreams[p].flush();

            }
            for (int s = 0; s < sequenceList.size(); s++) {
                int[] sequence = sequenceList.get(s);
                for (int p = 0; p < numberOfPlayers; p++) {
                    for (int num : sequence) {
                        outputStreams[p].write((num + "\n").getBytes()); // graph情報
                        outputStreams[p].flush();
                    }
                }
                GraphDrawing gui = null;
                if (isVisible) {
                    gui = new GraphDrawing(10, 10, graph.getNodeWeight(), names);
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
                            throw new NumberFormatException(
                                    "次のプレイヤーから整数以外の値を取得しました :" + names[p] + " :" + outputStr[p]);
                        }
                        gain(p, num, gameRecord[i][s], names[p]);

                        gameRecord[i][s][num][1] = j;
                        for (int pp : sequence) {
                            if (pp == p)
                                continue;
                            outputStreams[pp].write((num + "\n").getBytes());
                            outputStreams[pp].flush();
                        }
                        if (isVisible) {
                            gui.setColor(num / 10, num % 10, p);
                            try {
                                Thread.sleep(100);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }

                    }

                }

                if (outputLevel >= 3) {
                    for (int a = 0; a < 10; a++) {
                        for (int b = 0; b < 10; b++) {
                            System.out.printf("%2d ", gameRecord[i][s][a * 10 + b][0]);
                        }
                        System.out.println();
                    }
                }

                // 勝ち点の計算
                int[] gainNodeInfo = new int[gameRecord[i][s].length];
                for (int t = 0; t < gainNodeInfo.length; t++) {
                    gainNodeInfo[t] = gameRecord[i][s][t][0];
                }
                gainRecord[i][s] = graph.evaluate(gainNodeInfo, numberOfPlayers, numberOfSelectNodes);
                int[] gamePoint = calcPoint(gainRecord[i][s]);
                if (outputLevel >= 2) {
                    System.out.printf("%2dゲーム，順列種%2d番の利得 (", i, s);
                    for (String name : names) {
                        System.out.print(name + " ");
                    }
                    System.out.print(") = ");
                    for (int num : gainRecord[i][s]) {
                        System.out.print(num + " ");
                    }
                    System.out.print(" | 点数: ");
                    for (int num : gamePoint) {
                        System.out.print(num + " ");
                    }
                    System.out.println();

                }
                for (int t = 0; t < numberOfPlayers; t++) {
                    playerPoints[t] += gamePoint[t];
                }
                if (isVisible) {
                    gui.setColor(graph.getPlaneGain());
                    try {
                        Thread.sleep(500);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    gui.dispose();
                }
            }

        }
        if (outputLevel > 0) {
            System.out.print("勝ち点合計:");
            for (int num : playerPoints) {
                System.out.print(num + " ");
            }
            System.out.println();
        }
        return new Result(names, playerPoints);
    }

    private int[] calcPoint(int[] gainRecord) {
        List<NumberPair> dict = new ArrayList<>();
        for (int i = 0; i < numberOfPlayers; i++) {
            dict.add(new NumberPair(i, gainRecord[i]));
        }
        dict.sort((a, b) -> b.num - a.num);
        int[] point = new int[numberOfPlayers];
        if (numberOfPlayers == 2) {
            NumberPair numpair = dict.get(0);
            if (numpair.num != dict.get(1).num) {
                point[numpair.key]++;
            }
        } else if (numberOfPlayers == 3) {
            int[] score = new int[] { 5, 2, 0 };
            int beforeNum = dict.get(0).num;
            int index = 0;
            NumberPair numpair = dict.get(0);
            point[numpair.key] = score[index];

            for (int i = 1; i < numberOfPlayers; i++) {
                numpair = dict.get(i);
                if (beforeNum != numpair.num) {
                    beforeNum = numpair.num;
                    index = i;
                }
                point[numpair.key] = score[index];

            }

        }
        return point;
    }

    class NumberPair {
        int key;
        int num;

        NumberPair(int key, int num) {
            this.key = key;
            this.num = num;
        }
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
        List<String> commandList = setting.getCommandList();
        String[] names = new String[commandList.size()];
        List<Result> resultList = new ArrayList<>();
        int[] matching = new int[numberOfPlayers];
        autoRun(commandList, names, resultList, matching, 0);
        result(names, resultList);
    }

    private void autoRun(List<String> commandList, String[] names, List<Result> resultList, int[] matching, int count) {
        if (numberOfPlayers == count) {
            // 対戦とリザルトの格納
            String[] commands = new String[numberOfPlayers];
            for (int i = 0; i < numberOfPlayers; i++) {
                commands[i] = commandList.get(matching[i]);
            }
            try {
                startSubProcess(commands);
                Result result = run();
                String[] resultNames = result.names;
                for (int i = 0; i < numberOfPlayers; i++) {
                    names[matching[i]] = resultNames[i];
                    result.setPlayerID(matching);
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
            return;
        }

        // matching[count]番目以降との組み合わせだけを考える
        for (int i = matching[count - 1] + 1; i < commandList.size(); i++) {
            matching[count] = i;
            autoRun(commandList, names, resultList, matching, count + 1);
        }
    }

    /**
     * リザルトの出力
     */
    private void result(String[] names, List<Result> resultList) {
        if (numberOfPlayers == 3) {
            System.out.println("RESULT");
            int[][][] resultArray = new int[names.length][names.length][names.length];
            for (Result result : resultList) {
                int[] id = result.playerID;
                int[] score = result.playerPoints;

                resultArray[id[0]][id[1]][id[2]] = score[0];
                resultArray[id[1]][id[0]][id[2]] = score[1];
                resultArray[id[2]][id[0]][id[1]] = score[2];
            }
            System.out.printf("%22s", "");
            for (int j = 0; j < names.length; j++) {
                for (int k = j + 1; k < names.length; k++) {
                    System.out.printf("(%d-%d)", j, k);
                }
            }
            System.out.println();
            for (int i = 0; i < names.length; i++) {
                System.out.printf("%3d,%18s ", i, names[i]);
                for (int j = 0; j < names.length; j++) {
                    for (int k = j + 1; k < names.length; k++) {
                        System.out.printf("%4d ", resultArray[i][j][k]);
                    }
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
            pw.printf(",");
            for (int j = 0; j < names.length; j++) {
                for (int k = j + 1; k < names.length; k++) {
                    pw.printf(",(%d-%d)", j, k);
                }
            }
            pw.println();
            for (int i = 0; i < names.length; i++) {
                pw.printf("%d,%s,", i, names[i]);
                for (int j = 0; j < names.length; j++) {
                    for (int k = j + 1; k < names.length; k++) {
                        pw.printf("%d,", resultArray[i][j][k]);
                    }
                }
                pw.println();
            }
            pw.close();
        } else if (numberOfPlayers == 2) {
            System.out.println("RESULT");
            int[][] resultArray = new int[names.length][names.length];
            for (Result result : resultList) {
                int[] id = result.playerID;
                int[] score = result.playerPoints;

                resultArray[id[0]][id[1]] = score[0];
                resultArray[id[1]][id[0]] = score[1];
            }
            System.out.printf("%23s", "");
            for (int j = 0; j < names.length; j++) {
                System.out.printf("%4d ", j);
            }
            System.out.println();
            for (int i = 0; i < names.length; i++) {
                System.out.printf("%3d,%18s ", i, names[i]);
                for (int j = 0; j < names.length; j++) {
                    System.out.printf("%4d ", resultArray[i][j]);
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
            pw.printf(",");
            for (int j = 0; j < names.length; j++) {
                pw.printf(",%d", j);
            }
            pw.println();
            for (int i = 0; i < names.length; i++) {
                pw.printf("%d,%s,", i, names[i]);
                for (int j = 0; j < names.length; j++) {
                    pw.printf("%d,", resultArray[i][j]);
                }
                pw.println();
            }
            pw.close();
        }

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
        int[] playerID;

        Result(String[] names, int[] playerPoints) {
            this.names = names;
            this.playerPoints = playerPoints;
        }

        void setPlayerID(int[] id) {
            playerID = new int[id.length];
            for (int i = 0; i < id.length; i++) {
                playerID[i] = id[i];
            }
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
