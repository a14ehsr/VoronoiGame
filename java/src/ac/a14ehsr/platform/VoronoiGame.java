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
import java.util.Collections;
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

    int timeLimit = 1000;
    int timeOut = timeLimit + 1000;

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
                        if (!processes[p].isAlive())
                            throw new IOException("次のプレイヤーのサブプロセスが停止しました :" + names[p]);
                        Thread thread = new GetResponseThread(p);
                        thread.start();
                        long start = System.nanoTime();
                        try {
                            thread.join(timeOut);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        long calculateTime = System.nanoTime() - start;
                        if (outputStr[p] == null)
                            throw new TimeoutException("一定時間以内に次のプレイヤーから値を取得できませんでした :" + names[p]);

                        int num;
                        try {
                            num = Integer.parseInt(outputStr[p]);
                            outputStr[p] = null;
                        } catch (NumberFormatException e) {
                            throw new NumberFormatException(
                                    "次のプレイヤーから整数以外の値を取得しました :" + names[p] + " :" + outputStr[p]);
                        }
                        gain(p, num, gameRecord[i][s], names[p], calculateTime);

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
                    for (int a = 0; a < numberOfPlayers; a++) {
                        System.out.print("["+sequence[a]+"]"+names[a] + " ");
                    }
                    
                    System.out.print(") = ");
                    for (int num : gainRecord[i][s]) {
                        System.out.printf("%3d ", num);
                    }
                    System.out.print(" | 点数: ");
                    for (int num : gamePoint) {
                        System.out.printf("%3d ", num);
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
        } else if (numberOfPlayers >= 3) {
            int[] score = null;
            if (numberOfPlayers == 3) {
                score = new int[] { 5, 2, 0 };
            } else if (numberOfPlayers == 4) {
                score = new int[] { 7, 4, 2, 0 };
            } else if (numberOfPlayers == 5) {
                score = new int[] { 8, 5, 3, 1, 0 };
            } else {
                score = new int[numberOfPlayers];
            }
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
    private void gain(int player, int node, int[][] record, String name, long calculateTime) throws AgainstTheRulesException {
        if (record[node][0] != -1) {
            throw new AgainstTheRulesException("次のプレイヤーが既に獲得されたノードを選択しました：" + name);
        }
        double overTime = calculateTime / 1.0e6 - timeLimit;
        if (overTime > 0) {
            throw new AgainstTheRulesException("次のプレイヤーが制限時間を"+String.format("%.3f",overTime)+"ミリ秒超えました．" + name);
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
                resultList.add(new Result(matching));
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

    private int makeIndexForResult(int[] id, int index, int size) {
        List<Integer> list = new ArrayList<>();
        for (int i = 0; i < id.length; i++) {
            if (i == index)
                continue;
            list.add(id[i]);
        }
        Collections.sort(list);

        int ans = 0;
        int count = id.length - 2;
        for (int num : list) {
            ans += num * Math.pow(size, count--);
        }
        return ans;
    }
    
    private void makePairString(List<String> strList, int size, int count, String str) {
        if (count == numberOfPlayers - 1) {
            strList.add(str);
            return;
        }
        for (int i = 0; i < size; i++) {
            makePairString(strList, size, count + 1, str + "-" + i);
        }
    }
    
    /**
     * リザルトの出力
     */
    private void result(String[] names, List<Result> resultList) {
        // 各順位を何回とったか集計
        int[][] rankCount = new int[names.length][numberOfPlayers+1];
        String[][] resultArray = new String[names.length][(int) Math.pow(names.length, numberOfPlayers - 1)];
        for(String[] array : resultArray){
            Arrays.fill(array, "null");
        }
        for (Result result : resultList) {
            int[] id = result.playerID;
            int[] rank = result.rank;
            for (int i = 0; i < numberOfPlayers; i++) {
                if (result.isNoContest) {
                    resultArray[id[i]][makeIndexForResult(id, i, names.length)] = "VOID";
                    rankCount[id[i]][numberOfPlayers]++;
                } else {
                    rankCount[id[i]][rank[i]]++;
                    resultArray[id[i]][makeIndexForResult(id, i, names.length)] = "" + (rank[i] + 1);
                }
            }
        }

        boolean[] skip = new boolean[resultArray[0].length];
        Arrays.fill(skip, true);
        for (int i = 0; i < resultArray[0].length; i++) {
            for (int j = 0; j < resultArray.length; j++) {
                if (!"null".equals(resultArray[j][i])) {
                    skip[i] = false;
                    break;
                }
            }
        }

        List<String> strList = new ArrayList<>();
        for (int i = 0; i < names.length; i++) {
            makePairString(strList, names.length, 1, ""+i);
        }
        System.out.println("RESULT");
        System.out.printf("%23s", "");
        for (int i=0; i<strList.size(); i++) {
            if (skip[i]) {
                continue;
            }
            System.out.printf("(%5s)", strList.get(i));
        }

        System.out.printf(" |");
        for (int i = 1; i <= numberOfPlayers; i++) {
            System.out.print(" r"+i);
        }
        System.out.println(" VOID (times)");
        for (int i = 0; i < names.length; i++) {
            System.out.printf("%3d:%18s ", i, names[i]);
            for (int j = 0; j < resultArray[i].length; j++) {
                if (skip[j]) {
                    continue;
                }
                System.out.printf("%6s ", resultArray[i][j]);
            }
            System.out.printf(" |");
            for (int j = 0; j < numberOfPlayers; j++) {
                System.out.printf(" %2d", rankCount[i][j]);
            }
            System.out.printf("   %2d\n", rankCount[i][numberOfPlayers]);
        }

        
        // リザルト出力用ファイルの準備
        FileWriter file = null;
        try {
            file = new FileWriter("resource/result/"+numberOfPlayers+"PlayersResult.csv");
        } catch (Exception e) {
            e.printStackTrace();
        }
        PrintWriter pw = new PrintWriter(new BufferedWriter(file));
        pw.printf(",");
        for (int i = 0; i < strList.size(); i++) {
            if (skip[i]) {
                continue;
            }
            pw.printf(",(%s)", strList.get(i));
        }
        for (int i = 1; i <= numberOfPlayers; i++) {
            pw.print(",r" + i);
        }
        pw.println(",VOID,(times)");
        for (int i = 0; i < names.length; i++) {
            pw.printf("%d,%s", i, names[i]);
            for (int j = 0; j < resultArray[i].length; j++) {
                if (skip[j]) {
                    continue;
                }
                pw.printf(",%s", resultArray[i][j]);
            }
            for (int j = 0; j < numberOfPlayers; j++) {
                pw.printf(",%d", rankCount[i][j]);
            }
            pw.printf(",%d\n", rankCount[i][numberOfPlayers]);
        }
        pw.close();
    }
    
    /**
    * テスト実行によるふるい
    */
    private void test() {
        List<String> commandList = setting.getCommandList();
        List<String> sampleCommandList = setting.getTestSampleCommandList();
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
        int[] rank;
        boolean isNoContest;

        Result(String[] names, int[] playerPoints) {
            this.names = names;
            this.playerPoints = playerPoints;
            rank = new int[names.length];
            setRank();
            isNoContest = false;
        }

        Result(int[] id) {
            playerID = new int[id.length];
            for (int i = 0; i < id.length; i++) {
                playerID[i] = id[i];
            }
            isNoContest = true;
        }

        void setPlayerID(int[] id) {
            playerID = new int[id.length];
            for (int i = 0; i < id.length; i++) {
                playerID[i] = id[i];
            }
        }

        /**
         * ランク情報をセットする
         * 任意のプレイヤー人数に対応済み
         */
        void setRank() {
            // プレイヤーIDと特典をペアにして特典順にソート
            List<NumberPair> dict = new ArrayList<>();
            for (int i = 0; i < names.length; i++) {
                dict.add(new NumberPair(i, playerPoints[i]));
            }
            dict.sort((a, b) -> b.num - a.num);

            int beforeNum = dict.get(0).num;
            int index = 0;
            NumberPair numpair = dict.get(0);
            // 初期化時点で0なので以下の処理は暗黙のうちに行われている.
            //rank[numpair.key] = 0;

            // 特典順に見て，同じ値の時は同じ順位をつけていく．
            for (int i = 1; i < numberOfPlayers; i++) {
                numpair = dict.get(i);
                if (beforeNum != numpair.num) {
                    beforeNum = numpair.num;
                    index = i;
                }
                rank[numpair.key] = index;
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
