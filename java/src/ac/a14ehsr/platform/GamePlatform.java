package ac.a14ehsr.platform;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import ac.a14ehsr.platform.config.Options;
import ac.a14ehsr.platform.visualizer.GameModeWindow;
import ac.a14ehsr.platform.visualizer.Visualizer;
import ac.a14ehsr.player.HumanPlayer;
import ac.a14ehsr.player.Player;
import javax.swing.JFrame;

public class GamePlatform implements Runnable{
    public Player[] players;
    Options setting;

    String[] withHumanCommand;
    Visualizer visualizer;
    int numberOfPlayers;

    JFrame frame;

    public static void main(String[] args) {
        (new GamePlatform()).go(args);
    }

    GamePlatform(){}

    public GamePlatform(int numberOfPlayers, String[] commands, Visualizer visualizer, JFrame frame) {
        withHumanCommand = commands;
        this.visualizer = visualizer;
        this.numberOfPlayers = numberOfPlayers;
        this.frame = frame;

    }
    
    private void go(String[] args) {
        setting = new Options();
        setting.start(args);
        if(setting.isTest()) {
            System.err.println("test");
            test();
        }else{
            System.err.println("not test");
            autoRun();
        }
    }

    @Override
    public void run() {
        try {
            startSubProcess(numberOfPlayers,withHumanCommand);
            battle(
                new TronBattle(numberOfPlayers, players, visualizer),
                3,
                true,
                4000);
        } catch(IOException e) {
            e.printStackTrace();   
        } finally {
            for(Player player : players) {
                player.destroy();
            }
        }

    }

    /**
    * テスト実行によるふるい
    */
    private void test() {
        List<String> commandList = setting.getCommandList();
        List<String> sampleCommandList = setting.getTestSampleCommandList();

        // 実行コマンド出力ファイルの準備
        FileWriter file = null;
        try {
            file = new FileWriter("resource/command_list/command_list_green.txt");
        } catch (Exception e) {
            e.printStackTrace();
        }
        PrintWriter pw = new PrintWriter(new BufferedWriter(file));

        // サンプルと対戦させ，例外が発生しなかれば，実行可能コマンドとしてファイルに出力
        for (int i = 0; i < commandList.size(); i++) {
            String playerCommand = commandList.get(i);
            System.out.println(playerCommand);
            try {
                for (String command : sampleCommandList) {
                    startSubProcess(setting.getNumberOfPlayers(),new String[] { playerCommand, command });
                    Result result = battle(new TronBattle(setting.getNumberOfPlayers(), setting.getNumberOfGames(), setting.getTimelimit(), setting.isVisible(), setting.getOutputLevel(), players, 10, 10), setting.getOutputLevel(), setting.isVisible(), setting.getTimelimit()+1000);
                    processDestroy();
                }
                pw.println(playerCommand);
            } catch (Exception e) {
                System.err.println(e);
            } finally {
                processDestroy();
            }
        }
        pw.close();
    }

    void processDestroy() {
        for(Player player : players) {
            player.destroy();
        }
    }

    /**
     * 対戦の実行
     */
    private void autoRun() {
        List<String> commandList = setting.getCommandList();
        String[] names = new String[commandList.size()];
        List<Result> resultList = new ArrayList<>();
        int[] matching = new int[setting.getNumberOfPlayers()];
        autoRun(commandList, names, resultList, matching, 0);
        result(names, resultList);
    }

    private void autoRun(List<String> commandList, String[] names, List<Result> resultList, int[] matching, int count) {
        int numberOfPlayers = setting.getNumberOfPlayers();
        if (numberOfPlayers == count) {
            // 対戦とリザルトの格納
            String[] commands = new String[numberOfPlayers];
            for (int i = 0; i < numberOfPlayers; i++) {
                commands[i] = commandList.get(matching[i]);
            }
            try {
                startSubProcess(numberOfPlayers,commands);
                Result result = battle(new TronBattle(numberOfPlayers, setting.getNumberOfGames(), setting.getTimelimit(), setting.isVisible(), setting.getOutputLevel(), players, 30, 20), setting.getOutputLevel(), setting.isVisible(), setting.getTimelimit()+1000);
                String[] resultNames = result.names;
                for (int i = 0; i < numberOfPlayers; i++) {
                    names[matching[i]] = resultNames[i];
                    result.setPlayerID(matching);
                }

                resultList.add(result);
            } catch (Exception e) {
                e.printStackTrace();
                resultList.add(new Result(matching));
            } finally {
                for(Player player : players) {
                    player.destroy();
                }
            }

            return;
        }
        if (count == 0) {
            for (int i = 0; i < commandList.size(); i++) {
                matching[0] = i;
                autoRun(commandList, names, resultList, matching, count + 1);
            }
            return;
        }

        // matching[count]番目以降との組み合わせだけを考える
        for (int i = matching[count - 1] + 1; i < commandList.size(); i++) {
            matching[count] = i;
            autoRun(commandList, names, resultList, matching, count + 1);
        }
    }

    private int makeIndexForResult(int[] id, int index, int size) {
        List<Integer> list = new ArrayList<>();
        for (int i = 0; i < id.length; i++) {
            if (i == index)
                continue;
            list.add(id[i]);
        }
        Collections.sort(list);

        int ans = 0;
        int count = id.length - 2;
        for (int num : list) {
            ans += num * Math.pow(size, count--);
        }
        return ans;
    }
    
    private void makePairString(List<String> strList, int size, int count, String str) {
        int numberOfPlayers = setting.getNumberOfPlayers();
        if (count == numberOfPlayers - 1) {
            strList.add(str);
            return;
        }
        for (int i = 0; i < size; i++) {
            makePairString(strList, size, count + 1, str + "-" + i);
        }
    }
    
    /**
     * リザルトの出力
     */
    private void result(String[] names, List<Result> resultList) {
        int numberOfPlayers = setting.getNumberOfPlayers();
        // 各順位を何回とったか集計
        int[][] rankCount = new int[names.length][numberOfPlayers+1];
        String[][] resultArray = new String[names.length][(int) Math.pow(names.length, numberOfPlayers - 1)];
        for(String[] array : resultArray){
            Arrays.fill(array, "null");
        }
        for (Result result : resultList) {
            int[] id = result.playerID;
            int[] rank = result.rank;
            for (int i = 0; i < numberOfPlayers; i++) {
                if (result.isNoContest) {
                    resultArray[id[i]][makeIndexForResult(id, i, names.length)] = "VOID";
                    rankCount[id[i]][numberOfPlayers]++;
                } else {
                    rankCount[id[i]][rank[i]]++;
                    resultArray[id[i]][makeIndexForResult(id, i, names.length)] = "" + (rank[i] + 1);
                }
            }
        }

        boolean[] skip = new boolean[resultArray[0].length];
        Arrays.fill(skip, true);
        for (int i = 0; i < resultArray[0].length; i++) {
            for (int j = 0; j < resultArray.length; j++) {
                if (!"null".equals(resultArray[j][i])) {
                    skip[i] = false;
                    break;
                }
            }
        }

        List<String> strList = new ArrayList<>();
        for (int i = 0; i < names.length; i++) {
            makePairString(strList, names.length, 1, ""+i);
        }
        System.out.println("RESULT");
        System.out.printf("%23s", "");
        for (int i=0; i<strList.size(); i++) {
            if (skip[i]) {
                continue;
            }
            System.out.printf("(%5s)", strList.get(i));
        }

        System.out.printf(" |");
        for (int i = 1; i <= numberOfPlayers; i++) {
            System.out.print(" r"+i);
        }
        System.out.println(" VOID (times)");
        for (int i = 0; i < names.length; i++) {
            System.out.printf("%3d:%18s ", i, names[i]);
            for (int j = 0; j < resultArray[i].length; j++) {
                if (skip[j]) {
                    continue;
                }
                System.out.printf("%6s ", resultArray[i][j]);
            }
            System.out.printf(" |");
            for (int j = 0; j < numberOfPlayers; j++) {
                System.out.printf(" %2d", rankCount[i][j]);
            }
            System.out.printf("   %2d\n", rankCount[i][numberOfPlayers]);
        }

        // リザルト出力用ファイルの準備
        FileWriter file = null;
        try {
            file = new FileWriter("resource/result/"+numberOfPlayers+"PlayersResult.csv");
        } catch (Exception e) {
            e.printStackTrace();
        }
        PrintWriter pw = new PrintWriter(new BufferedWriter(file));
        pw.printf(",");
        for (int i = 0; i < strList.size(); i++) {
            if (skip[i]) {
                continue;
            }
            pw.printf(",(%s)", strList.get(i));
        }
        for (int i = 1; i <= numberOfPlayers; i++) {
            pw.print(",r" + i);
        }
        pw.println(",VOID,(times)");
        for (int i = 0; i < names.length; i++) {
            pw.printf("%d,%s", i, names[i]);
            for (int j = 0; j < resultArray[i].length; j++) {
                if (skip[j]) {
                    continue;
                }
                pw.printf(",%s", resultArray[i][j]);
            }
            for (int j = 0; j < numberOfPlayers; j++) {
                pw.printf(",%d", rankCount[i][j]);
            }
            pw.printf(",%d\n", rankCount[i][numberOfPlayers]);
        }
        pw.close();
    }
    

    /**
     * サブプロセスの起動
     * @param numberOfPlayer
     * @param commands 実行コマンド
     * @throws IOException
     */
    public void startSubProcess(int numberOfPlayer, String[] commands) throws IOException {
        Runtime rt = Runtime.getRuntime();
        players = new Player[numberOfPlayer];
        for(int p = 0; p < numberOfPlayer; p++) {
            if("-human".equals(commands[p])) {
                HumanPlayer hp = new HumanPlayer(p);
                players[p] = hp;
                frame.addKeyListener(hp);
            } else {
                players[p] = new Player(rt,commands[p],p);
            }
        }

        // サブプロセス起動が長い可能性を考慮しておく
        try {
            Thread.sleep(1000);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // TODO: 引数にgameを入れる
    public Result battle(Game game, int outputLevel, boolean isVisible, long timeout) throws IOException {
        game.sendGameInfo();
        
        game.showPlayers();
        
        game.setVisualizerName();

        for(int g = 0; g < game.getNumberOfGames(); g++) {
            game.initialize();
            aGame(game);

            game.showGameResult();

            for(Player player : players) {
                player.pointAddition();
            }
            game.visualizerReset();
        }
        game.dispose();
        
        return game.result();
    }

    public void aGame(Game game) throws IOException {
        // ゲームが終了するまで続行
        while(true) {
            game.play();
            if(game.isContinue()) {
                game.sendGameContinue();
            }else {
                game.sendGameFinish();
                break;
            }
        }
    }


}