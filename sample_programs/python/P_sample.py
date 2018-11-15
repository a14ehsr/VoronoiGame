import sys

playerName = "P_SamplePython"
numberOfPlayers = int(sys.stdin.readline())
numberOfGames = int(sys.stdin.readline())
numberOfSelectNodes = int(sys.stdin.readline())
patternSize = int(sys.stdin.readline())
playerCode = int(sys.stdin.readline())
numberOfNodes = int(sys.stdin.readline())
numberOfEdges = int(sys.stdin.readline())
edges = [[0,0]*numberOfEdges]
for i in range(numberOfEdges):
    edges[i][0] = int(sys.stdin.readline())
    edges[i][1] = int(sys.stdin.readline())

print(playerName, flush = True);

def select(record, game) :
    while True :
        selectNode = (int) (Math.random() * numberOfNodes);
        if record[game][selectNode] != -1 :
            return selectNode;

gameRecord = [[[[-1,-1]*numberOfSelectNodes]*patternSize]*numberOfGames]

# ゲーム数ループ
for i in range(numberOfGames):
    sequence = []
    for j in range(numberOfPlayers):
        sequence.append(int(sys.stdin.readline()))

    gameRecord.append()
    for s in range(patternSize):
        for j in range(numberOfSelectNodes):
            for p in sequence:
                if p == playerCode:
                    selectNode = select(gameRecord, i)
                    print(selectNode, flush = True)
                else:
                    selectNode = int(sys.stdin.readline())
                
                gameRecord[i][s][selectNode][0] = p
                gameRecord[i][s][selectNode][1] = j


