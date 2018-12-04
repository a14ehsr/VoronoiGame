import sys,random

playerName = "P_SamplePython"
numberOfPlayers = int(sys.stdin.readline())
numberOfGames = int(sys.stdin.readline())
numberOfSelectNodes = int(sys.stdin.readline())
patternSize = int(sys.stdin.readline())
playerCode = int(sys.stdin.readline())
numberOfNodes = 0
numberOfEdges = 0
edges = []
weight = []
print(playerName, flush=True);

def loadGraph():
    global numberOfNodes
    global numberOfEdges
    global weight
    numberOfNodes = int(sys.stdin.readline())
    numberOfEdges = int(sys.stdin.readline())
    weight = [int(sys.stdin.readline()) for i in range(numberOfNodes)]
    edges = [[int(sys.stdin.readline()), int(sys.stdin.readline())] for i in range(numberOfEdges)]
    
def select(record, game, s, sequence):
    while True:
        node = int(random.random()*numberOfNodes)
        if record[game][s][node][0] == -1:
            return node
gameRecord = []
# ゲーム数ループ
for i in range(numberOfGames):
    loadGraph()
    gameRecord.append([[[-1,-1] for a in range(numberOfNodes)] for b in range(patternSize)])
    for s in range(patternSize):
        sequence = []
        for j in range(numberOfPlayers):
            sequence.append(int(sys.stdin.readline()))        
        for j in range(numberOfSelectNodes):
            for p in sequence:
                selectNode = -1
                if p == playerCode:
                    selectNode = select(gameRecord, i,s, sequence)
                    print(selectNode, flush = True)
                else:
                    selectNode = int(sys.stdin.readline())
                gameRecord[i][s][selectNode][0] = p
                gameRecord[i][s][selectNode][1] = j