import java.util.Scanner;
import java.util.List;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Arrays;

public class P_SampleJava {
    private int numberOfPlayers;
    private int numberOfGames;
    private int numberOfSelectNodes; // 1ゲームで選択するノード
    private int numberOfNodes;
    private int numberOfEdges;
    private int patternSize;
    private int playerCode; // 0始まりの識別番号
    private int[][] edges;
    private int[] weight;
    private Scanner sc;
    static final String playerName = "P_0";

    /**
     * 書き換え箇所．ノード選択のAI
     * 
     * @param record record[i][j]:ゲームiのノードjの獲得プレイヤーID．未獲得は-1
     * @param game   ゲーム数
     * @return 選択ノード番号
     */
    private int select(int[][][] record, int game) {
        while (true) {
            int selectNode = (int) (Math.random() * numberOfNodes);
            if (record[game][selectNode][0] == -1) {
                return selectNode;
            }
        }

    }

    public static void main(String[] args) {
        (new P_SampleJava()).run();
    }

    /**
     * ゲーム実行メソッド
     */
    public void run() {
        sc = new Scanner(System.in);
        initialize();

        int[][][] gameRecord = new int[numberOfGames][][];

        // ゲーム数ループ
        for (int i = 0; i < numberOfGames; i++) {
            loadGraph();
            gameRecord[i] = new int[numberOfNodes][2];
            for (int[] num : gameRecord[i]) {
                Arrays.fill(num,-1);
            }
            for (int s=0; s<patternSize; s++) {

                List<Integer> sequence = new LinkedList<Integer>();
                for (int j = 0; j < numberOfPlayers; j++) {
                    sequence.add(sc.nextInt());
                }
                //sequence.forEach(System.err::print);

                // 選択ノード数分のループ
                for (int j = 0; j < numberOfSelectNodes; j++) {

                    for (int p : sequence) {
                        int selectNode;
                        if (p == playerCode) {
                            selectNode = select(gameRecord, i);
                            System.out.println(selectNode);
                        } else {
                            selectNode = sc.nextInt();
                        }
                        gameRecord[i][selectNode][0] = p;
                        gameRecord[i][selectNode][1] = j;
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

    /**
     * グラフの読み込み ノード数，辺数，辺の情報（ノードA ノードB）の入力
     */
    private void loadGraph() {
        numberOfNodes = sc.nextInt();
        numberOfEdges = sc.nextInt();
        weight = new int[numberOfNodes];
        for (int i = 0; i < numberOfNodes; i++) {
            weight[i] = sc.nextInt();
        }
        edges = new int[numberOfEdges][2];
        for (int i = 0; i < numberOfEdges; i++) {
            edges[i][0] = sc.nextInt();
            edges[i][1] = sc.nextInt();
        }
    }

}
