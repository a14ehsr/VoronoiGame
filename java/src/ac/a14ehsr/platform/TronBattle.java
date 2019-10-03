package ac.a14ehsr.platform;

import java.io.IOException;
import java.util.Arrays;

import ac.a14ehsr.exception.TimeoutException;
import ac.a14ehsr.platform.visualizer.Visualizer;
import ac.a14ehsr.player.HumanPlayer;
import ac.a14ehsr.player.Player;

import java.awt.Color;


public class TronBattle extends Game {
    public static final int DEATH = 0;
    public static final int ALIVE = 1;

    public static final int UP = 1;
    public static final int RIGHT = 2;
    public static final int DOWN = 3;
    public static final int LEFT = 4;

    private static final int NOT_ACHIEVED = -1;
    private static final int WALL = -2;



    private int width;
    private int height;
    private int[][] board; // 1 <= x <= width, 1 <= y <= height
    private int[][] nowPosition; // position of players. nowPosition[player] = {x,y}
    private int[][] initialPosition; // position of players. nowPosition[player] = {x,y}
    private int aliveCount;
    private int deathCount;

    public TronBattle(int numberOfPlayers, int numberOfGames, long timelimit, boolean isVisible, int outputLevel, Player[] players, int width, int height) {
        super(numberOfPlayers, numberOfGames, timelimit, isVisible, outputLevel, players);
        this.width = width;
        this.height = height;
        board = makeBoard();
        //*
        if(isVisible) {
            setVisualizer(new Visualizer(width, height));
        }
        //*/
    }

    public TronBattle(int numberOfPlayers, Player[] players, Visualizer visualizer) {
        super(numberOfPlayers, 1, 3000, true, 3, players);
        this.width = 30;
        this.height = 20;
        board = makeBoard();
        setVisualizer(visualizer);
    }

    /**
     * 盤を作成する
     * 四辺は壁とする
     */
    public int[][] makeBoard() {
        int[][] board = new int[height + 2][width + 2];
        for(int[] a : board) {
            Arrays.fill(a, NOT_ACHIEVED);
        }
        for(int x = 0; x <= width+1; x++) {
            board[0][x] = WALL;
            board[height + 1][x] = WALL;
        }
        for(int y = 0; y <= height+1; y++) {
            board[y][0] = WALL;
            board[y][width + 1] = WALL;
        }
        return board;
    }

    @Override
    void initialize() throws IOException {
        board = makeBoard();
        initialPosition = new int[numberOfPlayers][2];
        nowPosition = new int[numberOfPlayers][2];
        for(int p = 0; p < numberOfPlayers; p++) {
            int[] tmp = new int[]{-1,-1};
            boolean check;
            do {
                check = true;
                tmp = new int[]{(int)(Math.random() * width) + 1, (int)(Math.random() * height) + 1};
                for(int k = 0; k < p; k++) {
                    if(tmp[0] == nowPosition[k][0] && tmp[1] == nowPosition[k][1]) {
                        check = false;
                        break;
                    }
                }
            } while(!check);
            initialPosition[p] = tmp.clone();
            nowPosition[p] = tmp.clone();
            if(isVisible) {
                int code = players[p].getCode();
                visualizer.setColor(code, nowPosition[code][0], nowPosition[code][1]);
                visualizer.setBorder(nowPosition[code][0], nowPosition[code][1], Color.WHITE, 3);
                visualizer.resetNameColor();
            }
            board[tmp[1]][tmp[0]] = players[p].getCode();

        }
        for(Player player : players) {
            player.setStatus(ALIVE);
            player.setGamePoint(numberOfPlayers);
            //*
            if(player.isHuman()) {
                ((HumanPlayer)player).setBoard(board, nowPosition);
            }
            //*/
        }
        aliveCount = numberOfPlayers;
        deathCount = 0;
    }

    @Override
    boolean isContinue() {
        if(numberOfPlayers == 1) {
            return players[0].getStatus() != DEATH;
        }
        return Arrays.stream(players).filter(player -> player.getStatus() == ALIVE).count() > 1;
    }

    @Override
    void sendGameInfo() throws IOException {
        super.sendGameInfo();
        for(Player player : players) {
            player.sendNum(width);
            player.sendNum(height);
        }
    }

    String positionToString(Player player) {
        int[] position = nowPosition[player.getCode()];
        return String.format("%10s:(%2d %2d)", player.getName(), position[0], position[1]);
    }

    @Override
    void play() {
        for(int p = 0; p < numberOfPlayers; p++) {
            Player player = players[p];
            if(player.getStatus() == DEATH) {
                continue;
            }
            for(int k = 0; k < numberOfPlayers; k++) {
                try{
                    player.sendNumArray(getPlayerSendNumPair(players[k]));
                } catch(IOException e) {
                    System.out.println("送信時エラー");
                    e.printStackTrace();
                    kill(players[k]);
                }
            }
            if(outputLevel == 3) {
                for(int k = 0; k < numberOfPlayers; k++) {
                    System.out.print(positionToString(players[k]) + " ");
                }
                System.out.println();
            }else if(outputLevel == 4) {
                show();
            }

            int code = player.getCode();

            if(isVisible) {
                visualizer.setNameBorder(p, Color.WHITE);
            }
            visualizer.setBorder(nowPosition[code][0], nowPosition[code][1], Color.WHITE, 6);

            int direction = put(player);
            if(nowPosition[code][0] != -1 && isVisible) {
                visualizer.setColor(code, nowPosition[code][0], nowPosition[code][1]);
                visualizer.setBorder(nowPosition[code][0], nowPosition[code][1], Color.WHITE, 3);
                try{
                    Thread.sleep(10);
                }catch(Exception e) {
                    e.printStackTrace();
                }
            }
            if(isVisible) {
                visualizer.setNameBorder(p, Color.BLACK);
            }
        }
        //Arrays.stream(players).forEach(p -> System.err.print(p.getGamePoint() + " "));
        //System.err.println();
        if(aliveCount == 1) {
            //System.err.println("優勝者判定");
            //Arrays.stream(players).filter(p -> p.getStatus() == ALIVE).forEach(System.out::println);
            int[] points = {1,3,6,10,15,25};
            Arrays.stream(players).filter(p -> p.getStatus() == ALIVE).forEach(p -> p.setGamePoint(points[deathCount]));
            //Arrays.stream(players).forEach(p -> System.err.print(p.getGamePoint() + " "));
        }
    }

    int[] getPlayerSendNumPair(Player player) {
        if(player.getStatus() == DEATH) {
            //System.err.println("p:" + player.getCode() + " :  {-1}");
            return new int[]{-1,-1,-1,-1};
        }
        int pCode = player.getCode();
        return new int[]{initialPosition[pCode][0], initialPosition[pCode][1], nowPosition[pCode][0], nowPosition[pCode][1]};
    }

    int put(Player player) {
        if(player.getStatus() == DEATH) {
            //System.err.println("もともと死んでる");
            return DEATH;
        }
        int direction = DEATH;
        try {
            direction = player.receiveNum(timelimit+1000, timelimit);
        }catch(Exception e) {
            e.printStackTrace();
            kill(player);
            return DEATH;
        }

        int playerCode = player.getCode();
        int x = nowPosition[playerCode][0];
        int y = nowPosition[playerCode][1];
        switch(direction) {
            case UP:
                y++;
                break;
            case DOWN:
                y--;
                break;
            case LEFT:
                x--;
                break;
            case RIGHT:
                x++;
                break;
            default:
                if(outputLevel >= 2){
                    System.out.println("存在しない移動パターンの値が入力されました　 | プレイヤー:" + player.getName());
                }
                kill(player);
                return DEATH;       
        }

        if(y < 0 || y > board.length || x < 0 || x > board[y].length || board[y][x] == WALL) {
            if(outputLevel >= 2){
                System.out.println("ボードの範囲外に移動しようとしました | プレイヤー:" + player.getName());
            }
            kill(player);
            return DEATH;
        }
        if(board[y][x] != NOT_ACHIEVED) {
            if(outputLevel >= 2){
                System.out.println("獲得済みのマスに移動しようとしました | プレイヤー:" + player.getName());
            }
            kill(player);
            return DEATH;
        }
        int code = player.getCode();
        board[y][x] = code;
        visualizer.setBorder(nowPosition[code][0], nowPosition[code][1], Color.BLACK, 1);
        nowPosition[player.getCode()] = new int[]{x,y};
        return direction;   
    }


    /**
     * playerに行動不能等の状態を付与
     * プレイヤーの現在座標を{-1,-1}に変更し,
     * プレイヤーが獲得した盤面を解放する
     * @param player
     */
    void kill(Player player) {
        int[] points = {1,3,6,10,15,25};
        player.setStatus(DEATH);
        player.setGamePoint(points[deathCount++]);
        aliveCount--;
        if(isVisible) {
            visualizer.setBorder(nowPosition[player.getCode()][0], nowPosition[player.getCode()][1], Color.BLACK, 1);
        }
        nowPosition[player.getCode()] = new int[]{-1,-1};
        if(isVisible) {
            try {
                Thread.sleep(300);
            }catch(InterruptedException e) {
                e.printStackTrace();
            }
        }
        deletePlayer(player);
    }

    @Override
    void calcGameResult() {
        for(Player player : players) {
            player.setSumPoint(player.getSumPoint() + (numberOfPlayers - player.getGamePoint()));
        }
    }

    @Override
    void show() {
        System.out.print("|");
        for(int x = 1; x <= width; x++) {
            System.out.print("---");
        }
        System.out.println("|");

        for(int y = 1; y <= height; y++) {
            System.out.print("|");
            for(int x = 1; x <= width; x++) {
                System.out.printf("%2d ",board[y][x]);
            }
            System.out.println("|");
        }

        System.out.print("|");
        for(int x = 1; x <= width; x++) {
            System.out.print("---");
        }
        System.out.println("|");
    }

    @Override
    void showGameResult() {
        if(outputLevel >= 2) {
            System.out.print("GAME POINT: ");
            for(Player player : players) {
                System.out.printf("%10s ",player.getName());
            }
            System.out.print(" | ");
            for(Player player : players) {
                System.out.printf("%2d ",player.getGamePoint());
            }
            System.out.println();
        }
    }

    void sendSize() throws IOException {
        for(Player player : players) {
            player.sendNum(width);
            player.sendNum(height);
        }
    }

    /**
     * playerが獲得した盤面を解放する
     * @param player
     */
    public void deletePlayer(Player player) {
        int code = player.getCode();
        for(int y = 1; y <= height; y++) {
            for(int x = 1; x <= width; x++) {
                if(board[y][x] == code) {
                    board[y][x] = -1;
                    if(isVisible) {
                        visualizer.relese(player.getCode(), x, y);
                        visualizer.setNameColor(player.getCode(), Color.BLACK);
                    }
                }
            }
        }
        if(isVisible) {
            visualizer.validate();
            try {
                Thread.sleep(300);
            }catch(InterruptedException e)  {
                e.printStackTrace();
            }
        }
    }

}