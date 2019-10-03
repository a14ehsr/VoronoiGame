package ac.a14ehsr.player;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

import ac.a14ehsr.exception.TimeoutException;
import ac.a14ehsr.exception.TimeoverException;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;



public class HumanPlayer extends Player implements KeyListener {
    /**
     * 文字列受け取り用の一時変数
     * 受け取ったらnullなどでリセットする
     */
    private String receiveString;

    private static final int CONTINUE  = 0;
    private static final int FINISH  = 1;

    private static final int DEATH = 0;
    private static final int UP = 1;
    private static final int RIGHT = 2;
    private static final int DOWN = 3;
    private static final int LEFT = 4;

    private static final int NOT_ACHIEVED = -1;

    private int numberOfPlayers;
    private int numberOfGames;
    private int timelimit; // millisecond
    private int width;
    private int height;
    //private int playerCode; // your player code. 0 <= playerCode < numberOfPlayers
    private String playerName = "Human"; // Do not include spaces in your name.
    private boolean inputApproval;
    private int direction;
    
    /**
     * currentPostion[p] = {p.x, p.y}
     * Payer p's current location is (p.x, p.y).
     */
    private int[][] currentPosition;

    /**
     * board[y][x] = status
     * Status is either NOT_ACHIEVED or player's code.
     * Be careful position status of location (x,y) saved board[y][x]
     * Array size is (height+2) * (width+2) bad Board size is height * width.
     * 1 <= x <= width, 1 <= y <= height.
     */
    private int[][] board;
    


    /**
     * @return the name
     */
    public String getName() {
        return playerName;
    }

    /**
     * @return the code
     */
    public int getCode() {
        return code;
    }

    /**
     * @return the gamePoint
     */
    public int getGamePoint() {
        return gamePoint;
    }

    /**
     * @return the rank
     */
    public int getRank() {
        return rank;
    }

    /**
     * @return the status
     */
    public int getStatus() {
        return status;
    }

    /**
     * @return the sumPoint
     */
    public int getSumPoint() {
        return sumPoint;
    }

    /**
     * @param gamePoint the gamePoint to set
     */
    public void setGamePoint(int gamePoint) {
        this.gamePoint = gamePoint;
    }

    public void pointAddition() {
        sumPoint += gamePoint;
        gamePoint = 0;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.playerName = name;
        //process.setName(name);
    }

    /**
     * @param rank the rank to set
     */
    public void setRank(int rank) {
        this.rank = rank;
    }

    /**
     * @param sumPoint the sumPoint to set
     */
    public void setSumPoint(int sumPoint) {
        this.sumPoint = sumPoint;
    }

    /**
     * @param status the status to set
     */
    public void setStatus(int status) {
        this.status = status;
    }

    public HumanPlayer(int code) throws IOException {
        //process = new PlayerProcess(runtime, runCommand, code);
        super(code);
        isHuman = true;
    }

    public void sendMes(String mes) throws IOException {
        //process.send(mes);
    }

    public void sendNum(int num) throws IOException {
        //process.send(num);
    }

    public void sendNumArray(int[] nums) throws IOException {
        //process.send(nums);
    } 

    public String receiveMes(long timeout, long timelimit) throws IOException, TimeoutException, TimeoverException {
        //return process.receiveMes(timeout, timelimit);
        return playerName;
    }

    public int receiveNum(long timeout, long timelimit) throws IOException, TimeoutException, TimeoverException {
        //return process.receiveNum(timeout, timelimit);
        direction = DEATH;
        inputApproval = true;
        System.err.println("HUMAN INPUT:" + code);
        Thread th = new Thread() {
            @Override
            public void run() {
                while(status != DEATH && direction == DEATH) {
                    System.err.print("");
                }           
            }
        };
        th.start();
        try {
            th.join(5000);
        } catch(Exception e) {
            e.printStackTrace();
        }
        inputApproval = false;
        return direction;
    }

    public void setBoard(int[][] board, int[][] currentPosition) {
        this.board = board;
        this.currentPosition = currentPosition;
    }

    public void destroy() {
        //process.destroy();
    }

    @Override
    public void keyReleased(KeyEvent e) {
        //System.err.println(e.getKeyChar());
    }

    @Override
    public void keyTyped(KeyEvent e) {
        //System.err.println(e.getKeyChar());   
    }
    @Override
    public void keyPressed(KeyEvent e) {
        System.err.println("HP code="+ code +" PRESS:"+e.getKeyCode());
        if(!inputApproval) return;
        int x = currentPosition[code][0];
        int y = currentPosition[code][1];
        if(code == 0) {
            switch(e.getKeyCode()) {
                case KeyEvent.VK_W:
                    if(y-1 == 0 || board[y-1][x] != NOT_ACHIEVED) return;
                    direction =  DOWN;
                    break;
                case KeyEvent.VK_D:
                    if(x+1 == width+1 || board[y][x+1] != NOT_ACHIEVED) return;
                    direction = RIGHT;
                    break;
                case KeyEvent.VK_S:
                    if(y+1 == height+1 || board[y+1][x] != NOT_ACHIEVED) return;
                    direction = UP;
                    break;
                case KeyEvent.VK_A:
                    if(x-1 == 0 || board[y][x-1] != NOT_ACHIEVED) return;
                    direction = LEFT;
                    break;
            }
        } else if(code == 1) {
            switch(e.getKeyCode()) {
                case KeyEvent.VK_UP:
                    if(y-1 == 0 || board[y-1][x] != NOT_ACHIEVED) return;
                    direction =  DOWN;
                    break;
                case KeyEvent.VK_RIGHT:
                    if(x+1 == width+1 || board[y][x+1] != NOT_ACHIEVED) return;
                    direction = RIGHT;
                    break;
                case KeyEvent.VK_DOWN:
                    if(y+1 == height+1 || board[y+1][x] != NOT_ACHIEVED) return;
                    direction = UP;
                    break;
                case KeyEvent.VK_LEFT:
                    if(x-1 == 0 || board[y][x-1] != NOT_ACHIEVED) return;
                    direction = LEFT;
                    break;
            }
        } else if(code == 2) {
            switch(e.getKeyCode()) {
                case KeyEvent.VK_U:
                    if(y-1 == 0 || board[y-1][x] != NOT_ACHIEVED) return;
                    direction =  DOWN;
                    break;
                case KeyEvent.VK_K:
                    if(x+1 == width+1 || board[y][x+1] != NOT_ACHIEVED) return;
                    direction = RIGHT;
                    break;
                case KeyEvent.VK_J:
                    if(y+1 == height+1 || board[y+1][x] != NOT_ACHIEVED) return;
                    direction = UP;
                    break;
                case KeyEvent.VK_H:
                    if(x-1 == 0 || board[y][x-1] != NOT_ACHIEVED) return;
                    direction = LEFT;
                    break;
            }
        }

        //System.err.println("dir-"+ direction);
    }
}