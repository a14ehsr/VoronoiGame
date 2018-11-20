package ac.a14ehsr.sample_ai;

import java.util.Scanner;
import java.util.stream.Collectors;
import java.util.List;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Arrays;

public class P_Chaise {
    private int numberOfPlayers;
    private int numberOfGames;
    private int numberOfSelectNodes; // 1ゲームで選択するノード
    private int numberOfNodes;
    private int numberOfEdges;
    private int patternSize;
    private int playerCode; // 0始まりの識別番号
    private int[][] edges;
    private int[][] weight;
    private Scanner sc;
    static final String playerName = "P_Chaise";

    /**
     * 書き換え箇所．ノード選択のAI
     * 
     * @param record record[i][j]:ゲームiのノードjの獲得プレイヤーID．未獲得は-1
     * @param game   ゲーム数
     * @return 選択ノード番号
     */
    private int select(int[][][][] record, List<NumPair> originalWeighList, int[][] value, int beforeNode, int game,
            int sequenceNumber) {
        if (beforeNode == -1) {
            int max = originalWeighList.stream().mapToInt(np -> np.num).max().getAsInt();
            List<NumPair> maxList = originalWeighList.stream().filter(np -> np.num == max)
                    .collect(Collectors.toCollection(ArrayList::new));
            return maxList.get((int) (Math.random() * maxList.size())).key;
        }
        List<NumPair> selectableNode = new ArrayList<>();
        int bcol = beforeNode % 10;
        int brow = beforeNode / 10;
        if (brow > 0) {
            selectableNode.add(new NumPair((brow - 1) * 10 + bcol, value[brow - 1][bcol]));
        }
        if (brow < 9) {
            selectableNode.add(new NumPair((brow + 1) * 10 + bcol, value[brow + 1][bcol]));
        }
        if (bcol > 0) {
            selectableNode.add(new NumPair(brow * 10 + bcol - 1, value[brow][bcol - 1]));
        }
        if (bcol < 9) {
            selectableNode.add(new NumPair(brow * 10 + bcol + 1, value[brow][bcol + 1]));
        }
        selectableNode.sort((a, b) -> b.num - a.num);
        for (NumPair np : selectableNode) {
            if (record[game][sequenceNumber][np.key][0] == -1) {
                return np.key;
            }
        }
        System.err.println("CCC");

        while (true) {
            int selectNode = (int) (Math.random() * numberOfNodes);
            if (record[game][sequenceNumber][selectNode][0] == -1) {
                return selectNode;
            }
        }

    }

    public static void main(String[] args) {
        (new P_Chaise()).run();
    }

    /**
     * ゲーム実行メソッド
     */
    public void run() {
        sc = new Scanner(System.in);
        initialize();

        int[][][][] gameRecord = new int[numberOfGames][][][];

        // ゲーム数ループ
        for (int i = 0; i < numberOfGames; i++) {
            loadGraph();
            gameRecord[i] = new int[patternSize][numberOfNodes][2];
            for (int[][] sequenceRecord : gameRecord[i]) {
                for (int[] nodeInfo : sequenceRecord) {
                    Arrays.fill(nodeInfo, -1);
                }
            }
            int[][] value = new int[10][10];
            calcValue(value);
            for (int s = 0; s < patternSize; s++) {

                List<Integer> sequence = new LinkedList<Integer>();
                for (int j = 0; j < numberOfPlayers; j++) {
                    sequence.add(sc.nextInt());
                }
                List<NumPair> originalWeightList = new ArrayList<>();
                for (int t = 0; t < numberOfNodes; t++) {
                    originalWeightList.add(new NumPair(t, value[t / 10][t % 10]));
                }
                originalWeightList.sort((a, b) -> b.num - a.num);
                int beforeNode = -1;
                // 選択ノード数分のループ
                for (int j = 0; j < numberOfSelectNodes; j++) {
                    
                    for (int p : sequence) {
                        int selectNode;
                        if (p == playerCode) {
                            selectNode = select(gameRecord,originalWeightList, value, beforeNode, i, s);
                            System.out.println(selectNode);

                            decreaseValue(value, selectNode / 10, selectNode % 10);
                            originalWeightList.clear();
                            for (int t = 0; t < numberOfNodes; t++) {
                                originalWeightList.add(new NumPair(t, value[t / 10][t % 10]));
                            }
                            originalWeightList.sort((a, b) -> b.num - a.num);

                        } else {
                            selectNode = sc.nextInt();
                            beforeNode = selectNode;
                        }
                        gameRecord[i][s][selectNode][0] = p;
                        gameRecord[i][s][selectNode][1] = j;

                        /*
                         * for (int a = 0; a < 10; a++) { for (int b = 0; b < 10; b++) {
                         * System.err.printf("%2d ", gameRecord[i][s][a * 10 + b][0]); }
                         * System.err.println(); } System.err.println();
                         */
                    }
                }
            }
        }
    }

    /**
     *
    
     */
    private void initialize() {
        numberOfPlayers = sc.nextInt();
        numberOfGames = sc.nextInt();
        numberOfSelectNodes = sc.nextInt();
        patternSize = sc.nextInt();
        playerCode = sc.nextInt();
        System.out.println(playerName);
    }

    private void decreaseValue(int[][] value, int row, int col) {
        if (row > 0) {
            value[row - 1][col] -= weight[row][col];
        }
        if (row < 9) {
            value[row + 1][col] -= weight[row][col];
        }
        if (col > 0) {
            value[row][col - 1] -= weight[row][col];
        }
        if (col < 9) {
            value[row][col + 1] -= weight[row][col];
        }
    }

    private void calcValue(int[][] value) {
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                value[i][j] = 2 * weight[i][j];
                if (i > 0) {
                    value[i][j] += weight[i - 1][j];
                }
                if (i < 9) {
                    value[i][j] += weight[i + 1][j];
                }
                if (j > 0) {
                    value[i][j] += weight[i][j - 1];
                }
                if (j < 9) {
                    value[i][j] += weight[i][j + 1];
                }
            }
        }
    }

    /**
     * グラフの読み込み ノード数，辺数，辺の情報（ノードA ノードB）の入力
     */
    private void loadGraph() {
        numberOfNodes = sc.nextInt();
        numberOfEdges = sc.nextInt();
        weight = new int[10][10];
        for (int i = 0; i < numberOfNodes; i++) {
            weight[i / 10][i % 10] = sc.nextInt();
        }
        edges = new int[numberOfEdges][2];
        for (int i = 0; i < numberOfEdges; i++) {
            edges[i][0] = sc.nextInt();
            edges[i][1] = sc.nextInt();
        }
    }

    class NumPair {
        int key, num;

        NumPair(int key, int num) {
            this.key = key;
            this.num = num;
        }
    }

}
