package ac.a14ehsr.platform;

import java.io.IOException;
import java.util.Arrays;

import ac.a14ehsr.exception.TimeoutException;
import ac.a14ehsr.platform.visualizer.Visualizer;
import ac.a14ehsr.player.Player;

public abstract class Game {
    protected int numberOfPlayers;
    protected int numberOfGames;
    protected long timelimit;
    protected Player[] players;
    protected boolean isVisible;
    protected int outputLevel;
    protected Visualizer visualizer;

    private static final int CONTINUE  = 0;
    private static final int FINISH  = 1;


    public Game(int numberOfPlayers, int numberOfGames, long timelimit, boolean isVisible, int outputLevel, Player[] players){
        this.numberOfGames = numberOfGames;
        this.numberOfPlayers = numberOfPlayers;
        this.timelimit = timelimit;
        this.players = players;
        this.isVisible = isVisible;
        this.outputLevel = outputLevel;
        visualizer = null;
    }

    public void setVisualizerName() {
        if(visualizer != null) {
            visualizer.setName(Arrays.stream(players).map(Player::getName).toArray(String[]::new));
        }
    }

    /**
     * @param visualizer the visualizer to set
     */
    public void setVisualizer(Visualizer visualizer) {
        this.visualizer = visualizer;
    }

    /**
     * ゲームを開始する時の一番最初の処理
     * ゲームごとに変わるプレイヤーに渡すべき情報があればここに出力を加える
     */
    abstract void initialize() throws IOException;

    /**
     * ゲーム状況を出力する
     */
    abstract void show();

    /**
     * ゲーム終了時の状況を出力
     */
    abstract void showGameResult();
    /**
     * ターン処理
     */
    abstract void play();

    /**
     * 順位付けを行い，それに合わせたポイントを付与する
     */
    void gameRank() {

    }

    /**
     * ゲーム終了時のplayerのgamePointから算出したポイントをsumPointに加算
     */
    abstract void calcGameResult();

    /**
     * @return the numberOfGames
     */
    public int getNumberOfGames() {
        return numberOfGames;
    }

    /**
     * @return the numberOfPlayers
     */
    public int getNumberOfPlayers() {
        return numberOfPlayers;
    }

    /**
     * @param numberOfGames the numberOfGames to set
     */
    public void setNumberOfGames(int numberOfGames) {
        this.numberOfGames = numberOfGames;
    }

    /**
     * @param numberOfPlayers the numberOfPlayers to set
     */
    public void setNumberOfPlayers(int numberOfPlayers) {
        this.numberOfPlayers = numberOfPlayers;
    }

    
    /**
     * playerのsumPointの高い順に順位を付与して
     * Resultオブジェクトを返す
     */
    Result result() {
        Result result = new Result(players);
        if(outputLevel > 0) {
            result.show();
        }
        return result;
    }

    void sendGameInfo() throws IOException {
        for(Player player : players) {
            player.sendNum(numberOfPlayers);
            player.sendNum(numberOfGames);
            player.sendNum((int)timelimit);
            player.sendNum(player.getCode());
            try{
                player.setName(player.receiveMes(1000, 1000));
            }catch(Exception e) {
                System.err.println("SEND GAME INFO ERROR");
                e.printStackTrace();
            }
        }
    }

    void showPlayers() {
        if (outputLevel > 0) {
            System.out.println("players  : " + String.join(" vs ",Arrays.stream(players).map(Player::getName).toArray(String[]::new)));
        }
    }

    /**
     * ゲームが終了しているならfalse
     */ 
    abstract boolean isContinue();

    void sendGameFinish() throws IOException {
        for(Player player : players) {
            if(player.getStatus() == 0) continue;
            player.sendNum(FINISH);
        }
    }

    void sendGameContinue() throws IOException {
        for(Player player : players) {
            if(player.getStatus() == 0) continue;
            player.sendNum(CONTINUE);
        }
    }

    void dispose() {
        if(visualizer != null) {
            visualizer.dispose();
        }
    }

    void visualizerReset() {
        if(visualizer != null) {
            visualizer.reset();
        }
    }
}