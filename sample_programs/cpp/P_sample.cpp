#include <iostream>
#include <stdlib.h>
int numberOfPlayers;
int numberOfGames;
int numberOfSelectNodes; // 1ゲームで選択するノード
int numberOfNodes;
int numberOfEdges;
int patternSize;
int playerCode; // 0始まりの識別番号
int **edges;
int *weight;
std::string playerName = "P_SampleCpp";

void initialize();
void loadGraph();
int select(int ****gameRecord, int game, int sequenceNumber, int *sequence);
int main(void)
{
    initialize();

    int ****gameRecord = new int ***[numberOfGames];

    // ゲーム数ループ
    for (int i = 0; i < numberOfGames; i++)
    {
        loadGraph();
        gameRecord[i] = new int **[patternSize];
        for (int a = 0; a < patternSize; a++)
        {
            gameRecord[i][a] = new int *[numberOfNodes];
            for (int b = 0; b < numberOfNodes; b++)
            {
                gameRecord[i][a][b] = new int[2];
                gameRecord[i][a][b][0] = -1;
                gameRecord[i][a][b][1] = -1;
            }
        }
        for (int s = 0; s < patternSize; s++)
        {

            int *sequence = new int[numberOfPlayers];
            for (int j = 0; j < numberOfPlayers; j++)
            {
                std::cin >> sequence[j];
            }

            // 選択ノード数分のループ
            for (int j = 0; j < numberOfSelectNodes; j++)
            {
                for (int pi = 0; pi < numberOfPlayers; pi++)
                {
                    int p = sequence[pi];
                    int selectNode;
                    if (p == playerCode)
                    {
                        selectNode = select(gameRecord, i, s, sequence);
                        std::cout << selectNode << std::endl;
                    }
                    else
                    {
                        std::cin >> selectNode;
                    }
                    gameRecord[i][s][selectNode][0] = p;
                    gameRecord[i][s][selectNode][1] = j;
                }
            }
        }
    }
}
int select(int ****record, int game, int sequenceNumber, int *sequence)
{
    while (true)
    {
        int selectNode = rand() % numberOfNodes;
        if (record[game][sequenceNumber][selectNode][0] == -1)
        {
            return selectNode;
        }
    }
}

/**
 * 初期化
 */
void initialize()
{
    std::cin >> numberOfPlayers;
    std::cin >> numberOfGames;
    std::cin >> numberOfSelectNodes;
    std::cin >> patternSize;
    std::cin >> playerCode;
    std::cout << playerName << std::endl;
}

/**
 * グラフの読み込み ノード数，辺数，辺の情報（ノードA ノードB）の入力
 */
void loadGraph()
{
    std::cin >> numberOfNodes;
    std::cin >> numberOfEdges;
    weight = new int[numberOfNodes];
    for (int i = 0; i < numberOfNodes; i++)
    {
        std::cin >> weight[i];
    }
    edges = new int *[numberOfEdges];
    for (int i = 0; i < numberOfEdges; i++)
    {
        edges[i] = new int[2];
        std::cin >> edges[i][0];
        std::cin >> edges[i][1];
    }
}
