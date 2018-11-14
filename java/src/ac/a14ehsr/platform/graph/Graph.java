package ac.a14ehsr.platform.graph;

public abstract class Graph {
    public int numberOfNodes;
    public int numberOfEdges;
    public int[] nodeWeight;
    public int[][] edges;

    Graph(int numberOfNodes, int numberOfEdges) {
        this.numberOfNodes = numberOfNodes;
        this.numberOfEdges = numberOfEdges;
        nodeWeight = new int[numberOfNodes];
        edges = new int[numberOfEdges][2];

    }

    public void printWeight() {

    }

    public int getNumberOfNodes() {
        return numberOfNodes;
    }

    public int[][] getPlaneGain() {
        return null;
    }

    public int[] evaluate(int[] gain, int numberOfPlayers, int numberOfSelectNodes) {
        int[] value = new int[numberOfPlayers];
        return value;
    }

    public int[] getNodeWeight() {
        return nodeWeight;
    }

    abstract void setWeight();

    abstract void setEdge();

    public String toString() {
        String str = numberOfNodes + "\n" + numberOfEdges + "\n";
        for (int weight : nodeWeight) {
            str += (weight + "\n");
        }
        for (int[] edge : edges) {
            str += (edge[0] + "\n" + edge[1] + "\n");
        }

        return str;
    }
}