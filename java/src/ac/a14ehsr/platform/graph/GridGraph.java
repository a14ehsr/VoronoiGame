package ac.a14ehsr.platform.graph;

import java.util.List;
import java.util.ArrayList;

public class GridGraph extends Graph {
    private int n;
    private int m;

    /**
     * コンストラクタ
     * 
     * @param n // 縦のサイズ
     * @param m // 横のサイズ
     */
    GridGraph(int n, int m) {
        super(n * m, m * (n - 1) + n * (m - 1));
        this.n = n;
        this.m = m;
        setWeight();
        setEdge();
    }

    /**
     * 重みの設定．それぞれの重みの設定するノード数は マジックナンバーとして記述してあるので，外部にするか割合にするかしたい．
     */
    @Override
    void setWeight() {
        List<Integer> unsetNodeList = new ArrayList<>();
        for (int i = 0; i < numberOfNodes; i++) {
            unsetNodeList.add(i);
        }
        int[][] numberOfNodeWeight = new int[5][];
        numberOfNodeWeight[0] = new int[] { 1, 10 };
        numberOfNodeWeight[1] = new int[] { 2, 20 };
        numberOfNodeWeight[2] = new int[] { 3, 40 };
        numberOfNodeWeight[3] = new int[] { 4, 20 };
        numberOfNodeWeight[4] = new int[] { 5, 10 };

        // 添え字iの重みのノードを順に決めていく
        for (int i = 0; i < numberOfNodeWeight.length; i++) {
            // 添え字iの重みを配置するノード数分のループ
            for (int j = 0; j < numberOfNodeWeight[i][1]; j++) {
                int index = (int) (Math.random() * unsetNodeList.size());
                int node = unsetNodeList.get(index);
                unsetNodeList.remove(index);
                nodeWeight[node] = numberOfNodeWeight[i][0];
            }
        }
    }

    /**
     * 辺の設定.
     */
    @Override
    void setEdge() {
        int edgeCount = 0;
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < m - 1; j++) {
                edges[edgeCount][0] = i * n + j;
                edges[edgeCount][1] = i * n + j + 1;
                edgeCount++;
            }
        }

        for (int j = 0; j < m; j++) {
            for (int i = 0; i < n - 1; i++) {
                edges[edgeCount][0] = i * n + j;
                edges[edgeCount][1] = (i + 1) * n + j;
                edgeCount++;
            }
        }
    }

    public static void main(String[] args) {
        Graph obj = new GridGraph(10, 10);
        System.out.println(obj);
    }
}