package ac.a14ehsr.platform;

import java.util.ArrayList;
import java.util.List;

import ac.a14ehsr.player.Player;
/**
 * リザルト用のEntity
 */
class Result {
    String[] names;
    int[] playerPoints;
    int[] playerID;
    int[] rank;
    boolean isNoContest;

    Result(Player[] players) {
        int numberOfPlayers = players.length;
        names = new String[numberOfPlayers];
        playerPoints = new int[numberOfPlayers];
        rank = new int[numberOfPlayers];
        for(int p = 0; p < numberOfPlayers; p++) {
            names[p] = players[p].getName();
            playerPoints[p] = players[p].getSumPoint();
        }
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
        for (int i = 1; i < names.length; i++) {
            numpair = dict.get(i);
            if (beforeNum != numpair.num) {
                beforeNum = numpair.num;
                index = i;
            }
            rank[numpair.key] = index;
        }
    }
    class NumberPair {
        int key;
        int num;
    
        NumberPair(int key, int num) {
            this.key = key;
            this.num = num;
        }
    }

    void show() {
        System.out.println("GAME RESULT: ");
        System.out.printf("%15s POINT RANK\n", "NAME");
        for(int i = 0; i < names.length; i++) {
            System.out.printf("%15s   %3d   %2d\n",names[i], playerPoints[i], rank[i]);
        }
    }
}
