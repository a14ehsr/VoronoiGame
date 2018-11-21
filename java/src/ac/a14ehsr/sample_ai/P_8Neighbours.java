package ac.a14ehsr.sample_ai;

import java.util.Scanner;
import java.util.stream.Collectors;
import java.util.List;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Arrays;

public class P_8Neighbours {
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
    static final String playerName = "P_8Neighbours";

    class NumPair {
        int key, num;

        NumPair(int key, int num) {
            this.key = key;
            this.num = num;
        }
    }

    public static void main(String[] args) {
        (new P_8Neighbours()).run();
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

                // 選択ノード数分のループ
                for (int j = 0; j < numberOfSelectNodes; j++) {

                    for (int p : sequence) {
                        int selectNode;
                        if (p == playerCode) {
                            NumPair np = null;
                            while (true) {
                                np = originalWeightList.remove(0);
                                if (gameRecord[i][s][np.key][0] == -1) {
                                    break;
                                }
                            }
                            selectNode = np.key;
                            System.out.println(selectNode);
                            decreaseValue(value, selectNode / 10, selectNode % 10);
                            originalWeightList.clear();
                            for (int t = 0; t < numberOfNodes; t++) {
                                originalWeightList.add(new NumPair(t, value[t / 10][t % 10]));
                            }
                            originalWeightList.sort((a, b) -> b.num - a.num);
                        } else {
                            selectNode = sc.nextInt();
                        }
                        gameRecord[i][s][selectNode][0] = p;
                        gameRecord[i][s][selectNode][1] = j;
                    }
                }
            }
        }
    }

    private void decreaseValue(int[][] value, int row,int col) {
        if (row > 0) {
            value[row - 1][col] -= 2 * weight[row][col];
            if (col > 0) {
                value[row - 1][col - 1] -= weight[row][col];
            }
            if (col < 9) {
                value[row - 1][col + 1] -= weight[row][col];
            }
        }
        if (row < 9) {
            value[row + 1][col] -= 2 * weight[row][col];
            if (col > 0) {
                value[row + 1][col - 1] -= weight[row][col];
            }
            if (col < 9) {
                value[row + 1][col + 1] -= weight[row][col];
            }
        }
        if (col > 0) {
            value[row][col - 1] -= 2 * weight[row][col];
        }
        if (col < 9) {
            value[row][col + 1] -= 2 * weight[row][col];
        }
    }

    private void calcValue(int[][] value) {
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                value[i][j] = 3 * weight[i][j];
                if (i > 0) {
                    value[i][j] += 2 * weight[i - 1][j];
                    if (j > 0) {
                        value[i][j] += weight[i - 1][j - 1];
                    }
                    if (j < 9) {
                        value[i][j] += weight[i - 1][j + 1];
                    }
                }
                if (i < 9) {
                    value[i][j] += 2 * weight[i + 1][j];
                    if (j > 0) {
                        value[i][j] += weight[i + 1][j - 1];
                    }
                    if (j < 9) {
                        value[i][j] += weight[i + 1][j + 1];
                    }
                }
                if (j > 0) {
                    value[i][j] += 2 * weight[i][j - 1];
                }
                if (j < 9) {
                    value[i][j] += 2 * weight[i][j + 1];
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

    /**
     * グラフの読み込み ノード数，辺数，辺の情報（ノードA ノードB）の入力
     */
    private void loadGraph() {
        numberOfNodes = sc.nextInt();
        numberOfEdges = sc.nextInt();
        weight = new int[10][10];
        for (int i = 0; i < numberOfNodes; i++) {
            weight[i/10][i%10] = sc.nextInt();
        }
        edges = new int[numberOfEdges][2];
        for (int i = 0; i < numberOfEdges; i++) {
            edges[i][0] = sc.nextInt();
            edges[i][1] = sc.nextInt();
        }
    }

}
