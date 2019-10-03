package ac.a14ehsr.platform.config;

import java.util.List;
import java.util.ArrayList;
import java.util.Scanner;
import java.io.File;
import java.io.IOException;

public class Options {
    private List<String> commandList;
    private List<String> sampleCommandList;
    private List<String> testSampleCommandList;
    private int numberOfGames;
    private int numberOfPlayers;
    private int outputLevel;
    private boolean test;
    private boolean visible;
    private long timelimit;

    /**
     * デフォルトコンストラクタ コマンドのリストの準備と設定ファイル読み込み
     */
    public Options() {
        commandList = new ArrayList<>();
        sampleCommandList = new ArrayList<>();
        testSampleCommandList = new ArrayList<>();

        try {
            defaultConfig();
        } catch (Exception e) {
            System.err.println("configファイルの様式が規定通りになっていません．");
            System.err.println("起動を中止します．");
            e.printStackTrace();
            System.exit(0);
        }
        String javaRunCommand = "";
        String javaRunOptions = "";
        try {
            Scanner tmpsc = new Scanner(new File("resource/config/java/run_command.txt"));
            javaRunCommand = tmpsc.next();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            Scanner tmpsc = new Scanner(new File("resource/config/java/run_options.txt"));
            if (tmpsc.hasNext()) {
                javaRunOptions = tmpsc.next();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        String common = javaRunCommand + " " + javaRunOptions + " -classpath java/src/ ac.a14ehsr.sample_ai.";
        
        sampleCommandList.add(common + "Ai_Clockwise");
        sampleCommandList.add(common + "Ai_Straight");
        sampleCommandList.add(common + "Ai_Random");
        /*
        sampleCommandList.add(common + "P_Max");
        sampleCommandList.add(common + "P_4Neighbours");
        sampleCommandList.add(common + "P_8Neighbours");
        sampleCommandList.add(common + "P_Chaise");
        sampleCommandList.add(common + "P_Copy");
        */

    }

    public List<String> getSampleCommandList() {
        return sampleCommandList;
    }

    public List<String> getCommandList() {
        return commandList;
    }

    public List<String> getTestSampleCommandList() {
        return testSampleCommandList;
    }

    /**
     * プレイヤー人数のgettter
     * 
     * @return
     */
    public int getNumberOfPlayers() {
        return numberOfPlayers;
    }

    /**
     * ゲーム数のgetter
     */
    public int getNumberOfGames() {
        return numberOfGames;
    }

    /**
     * 出力レベルのgetter
     */
    public int getOutputLevel() {
        return outputLevel;
    }

    public boolean isTest() {
        return test;
    }

    public boolean isVisible() {
        return visible;
    }
    
    public long getTimelimit(){
        return timelimit;
    }

    public long getTimeout() {
        return timelimit + 1000;
    }

    /**
     * デフォルトの設定を設定ファイルから読み込む
     * 
     * @throws Exception 設定ファイルの様式違いやその他のException
     */
    void defaultConfig() throws Exception {
        String configFilePath = "resource/config/config.txt";
        Scanner sc = null;
        try {
            sc = new Scanner(new File(configFilePath));
        } catch (Exception e) {
            e.printStackTrace();
        }
        test = false;
        visible = false;

        String[] line;
        while(sc.hasNextLine()) {
            line = sc.nextLine().split(" ");
            switch(line[0]) {
                case "players":
                    numberOfPlayers = Integer.parseInt(line[1]);
                    break;
                case "games":
                    numberOfGames = Integer.parseInt(line[1]);
                    break;
                case "timelimit":
                    timelimit = Long.parseLong(line[1]);
                    break;
                case "output":
                    outputLevel = Integer.parseInt(line[1]);
                    break;
            }
        }
    }

    /**
     * 設定処理を管理
     * 
     * @param args コマンドライン引数
     */
    public void start(final String[] args) {
        if (args.length > 0) {
            try {
                setOption(args);
            } catch (Exception e) {
                System.err.println(e);
                System.out.println("オプションがおかしいです．");
            }
        } else {
            System.out.println("オプションを指定してください．");
        }
        System.out.println("設定終了");
    }

    /**
     * コマンドライン引数を元に各設定を行う
     * 
     * @param options コマンドライン引数
     * @return 成功か否か
     * @throws Exception 範囲外アクセス，型変換例外
     */
    void setOption(final String[] options) throws NumberFormatException, ArrayIndexOutOfBoundsException, OptionsException {
        for (int i = 0; i < options.length;) {
            switch (options[i]) {
                case "-p":
                    commandList.add(options[i + 1]);
                    i += 2;
                    break;

                case "-nop":
                    numberOfPlayers = Integer.parseInt(options[i + 1]);
                    i += 2;
                    break;

                case "-game":
                    numberOfGames = Integer.parseInt(options[i + 1]);
                    i += 2;
                    break;

                case "-sample":
                    commandList.addAll(sampleCommandList);
                    i++;
                    break;

                case "-v":
                    visible = true;
                    i++;
                    break;

                case "-olevel":
                    int tmp = Integer.parseInt(options[i + 1]);
                    if (tmp > 4 || tmp < 1) {
                        System.out.println("出力モードは1,2,3のいずれかです．その他の値が入力されています．");
                        System.out.println("既定値で実行します．");
                    } else {
                        outputLevel = tmp;
                    }
                    i += 2;
                    break;

                case "-auto":
                    readCommandList(commandList, "resource/command_list/command_list.txt");
                    if ("true".equals(options[i + 1])) {
                        commandList.addAll(sampleCommandList);
                    }
                    i += 2;
                    break;
                /*
                case "-test":
                    numberOfGames = Integer.parseInt(options[i + 1]);
                    test = true;
                    System.err.println("test on");
                    readCommandList(commandList, "resource/command_list/command_list.txt");
                    outputLevel = 0;
                    i += 1;
                    break;
                */
                case "-human":
                // TODO: リストベタ打ち
                    readCommandList(commandList, "resource/command_list/command_list.txt");
                    i++;
                    break;

                default:
                    throw new OptionsException("存在しないオプション:"+options[i]);
            }
        }

    }

    void readCommandList(List<String> commandList, String fileName) {
        Scanner sc = null;
        try {
            sc = new Scanner(new File(fileName));
        } catch (Exception e) {
            e.printStackTrace();
        }
        while (sc.hasNext()) {
            commandList.add(sc.nextLine());
        }
        commandList.forEach(System.out::println);
    }

    class OptionsException extends Exception {
        /**
         * コンストラクタ
         * 
         * @param mes メッセージ
         */
        OptionsException(String mes) {
            super(mes);
        }
    }
}