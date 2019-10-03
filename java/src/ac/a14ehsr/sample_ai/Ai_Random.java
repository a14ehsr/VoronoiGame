package ac.a14ehsr.sample_ai;

import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;
import java.util.Scanner;

public class Ai_Random {
    private static final int CONTINUE  = 0;
    private static final int FINISH  = 1;

    private static final int DEATH = 0;
    private static final int UP = 1;
    private static final int RIGHT = 2;
    private static final int DOWN = 3;
    private static final int LEFT = 4;

    private static final int NOT_ACHIEVED = -1;
    private static final int WALL = -2;

    private int numberOfPlayers;
    private int numberOfGames;
    private int timelimit;
    private int width;
    private int height;
    private int playerCode; // 0始まりの識別番号
    private Scanner sc;
    static final String playerName = "S_Random";
    
    private int[][] nowPosition;
    private int[][] board;

    public static void main(String[] args) {
        (new Ai_Random()).run();
    }

        /**
     * ゲーム実行メソッド
     */
    public void run() {
        sc = new Scanner(System.in);
        initialize();

        // ゲーム数ループ
        for (int i = 0; i < numberOfGames; i++) {
            boolean continueFlag = true;

            nowPosition = new int[numberOfPlayers][2];
            board = new int[height + 2][width + 2];
            for(int[] a : board) {
                Arrays.fill(a, NOT_ACHIEVED);
            }

            while(continueFlag){
                for (int p = 0; p < numberOfPlayers; p++) {
                    int x0 = sc.nextInt();
                    int y0 = sc.nextInt();
                    int x1 = sc.nextInt();
                    int y1 = sc.nextInt();
                    //System.err.println(p + " | " +x0 + " " + y0 + " " + x1 + " " + y1);
                    move(p, x0, y0, x1, y1);
                }

                int direction = put();
                // 戦略を実行
                //direction = (int)(Math.random()*4) + 1;
                System.out.println(direction);

                if(sc.nextInt() == FINISH) {
                    continueFlag = false;
                }
            }
        }
    }

    public int put() {
        int x = nowPosition[playerCode][0];
        int y = nowPosition[playerCode][1];
        if(x == -1 && y == -1) {
            return DEATH;
        }
        List<Integer> directionList = new ArrayList<>();
        if(y < height && board[y+1][x] == NOT_ACHIEVED) {
            directionList.add(UP);
        }
        if(x < width && board[y][x+1] == NOT_ACHIEVED) {
            directionList.add(RIGHT);
        }
        if(y > 1 && board[y-1][x] == NOT_ACHIEVED) {
            directionList.add(DOWN);
        }
        if(x > 1 && board[y][x-1] == NOT_ACHIEVED) {
            directionList.add(LEFT);
        }
        if(directionList.size()==0) {
            return DEATH;
        }else{
            return directionList.get((int)(Math.random() * directionList.size()));
        }
    }
/**
     * Move player to point (moveX, moveY) and set first location.
     * @param player
     * @param initX
     * @param initY
     * @param moveX
     * @param moveY
     */
    public void move(int player, int initX, int initY, int moveX, int moveY) {
        if(moveX == -1) {
            for(int y = 1; y <= height; y++) {
                for(int x = 1; x <= width; x++) {
                    if(board[y][x] == player) {
                        board[y][x] = NOT_ACHIEVED;
                    }
                }
            }
            nowPosition[player][0] = -1;
            nowPosition[player][1] = -1;
            return;
        }
        board[initY][initX] = player;

        board[moveY][moveX] = player;
        nowPosition[player][0] = moveX;
        nowPosition[player][1] = moveY;
    }


    /**
     * 初期化
     */
    private void initialize() {
        numberOfPlayers = sc.nextInt();
        numberOfGames = sc.nextInt();
        timelimit = sc.nextInt();
        playerCode = sc.nextInt();

        System.out.println(playerName);

        width = sc.nextInt();
        height = sc.nextInt();
    }

}