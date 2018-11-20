package ac.a14ehsr.platform;

import java.io.PrintWriter;
import java.util.Scanner;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.io.File;
import java.io.IOException;

/**
 * プラットフォームのコマンドライン引数を受け取って諸々の処理をするクラス パラメータは
 * resource/setting/setting.txtから読み取る. 引数無し実行時には対話モードで動作する．
 */
class Setting {
    private List<String> commandList;
    private List<String> sampleCommandList;
    private int numberOfGames;
    private int numberOfSelectNodes;
    private int numberOfPlayers;
    private int outputLevel;
    private boolean isTest;
    private boolean visible;

    /**
     * デフォルトコンストラクタ コマンドのリストの準備と設定ファイル読み込み
     */
    Setting() {
        isTest = false;
        commandList = new ArrayList<>();
        sampleCommandList = new ArrayList<>();

        try {
            defaultSetting();
        } catch (Exception e) {
            System.err.println("settingファイルの様式が規定通りになっていません．");
            System.err.println("起動を中止します．");
            e.printStackTrace();
            System.exit(0);
        }
        String javaRunCommand = "";
        String javaRunOptions = "";
        try {
            Scanner tmpsc = new Scanner(new File("resource/setting/java/run_command.txt"));
            javaRunCommand = tmpsc.next();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            Scanner tmpsc = new Scanner(new File("resource/setting/java/run_options.txt"));
            if (tmpsc.hasNext()) {
                javaRunOptions = tmpsc.next();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        String common = javaRunCommand + " " + javaRunOptions + " -classpath java/src/ ac.a14ehsr.sample_ai.";
        sampleCommandList.add(common + "P_Random");
        sampleCommandList.add(common + "P_Max");
        sampleCommandList.add(common + "P_4Neighbours");
    }

    List<String> getSampleCommandList() {
        return sampleCommandList;
    }

    List<String> getCommandList() {
        return commandList;
    }

    /**
     * プレイヤー人数のgettter
     * 
     * @return
     */
    int getNumberOfPlayers() {
        return numberOfPlayers;
    }

    /**
     * ゲーム数のgetter
     */
    int getNumberOfGames() {
        return numberOfGames;
    }

    /**
     * 選択ノード数のgetter
     */
    int getNumberOfSelectNodes() {
        return numberOfSelectNodes;
    }

    /**
     * 出力レベルのgetter
     */
    int getOutputLevel() {
        return outputLevel;
    }

    boolean isTest() {
        return isTest;
    }

    boolean isVisible() {
        return visible;
    }

    /**
     * デフォルトの設定を設定ファイルから読み込む
     * 
     * @throws Exception 設定ファイルの様式違いやその他のException
     */
    void defaultSetting() throws Exception {
        String settingFilePath = "resource/setting/setting.txt";
        Scanner sc = null;
        try {
            sc = new Scanner(new File(settingFilePath));
        } catch (Exception e) {
            e.printStackTrace();
        }
        String[] line;
        line = sc.nextLine().split(" ");
        if (!"numberOfPlayers".equals(line[0])) {
            throw new Exception("line1 need be numberOfPlayers");
        }
        numberOfPlayers = Integer.parseInt(line[1]);

        line = sc.nextLine().split(" ");
        if (!"numberOfGames".equals(line[0])) {
            throw new Exception("line2 need be numberOfGames");
        }
        numberOfGames = Integer.parseInt(line[1]);

        line = sc.nextLine().split(" ");
        if (!"numberOfSelectNodes".equals(line[0])) {
            throw new Exception("line3 need be numberOfSelectNodes");
        }
        numberOfSelectNodes = Integer.parseInt(line[1]);

        line = sc.nextLine().split(" ");
        if (!"outputlevel".equals(line[0])) {
            throw new Exception("line4 need be outputlevel");
        }
        outputLevel = Integer.parseInt(line[1]);
        visible = false;
    }

    /**
     * 設定処理を管理
     * 
     * @param args コマンドライン引数
     */
    void start(final String[] args) {
        if (args.length > 0) {
            try {
                setOption(args);
            } catch (NumberFormatException e) {
                System.err.println(e);
                System.out.println("オプションがおかしいです．");
            } catch (ArrayIndexOutOfBoundsException e) {
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
    void setOption(final String[] options) throws NumberFormatException, ArrayIndexOutOfBoundsException {
        for (int i = 0; i < options.length; i += 2) {
            switch (options[i]) {
            case "-p":
                commandList.add(options[i + 1]);
                break;

            case "-nop":
                numberOfPlayers = Integer.parseInt(options[i + 1]);
                break;

            case "-nosn":
                numberOfSelectNodes = Integer.parseInt(options[i + 1]);
                break;

            case "-game":
                numberOfGames = Integer.parseInt(options[i + 1]);
                break;
            case "-v":
                if ("true".equals(options[i + 1])) {
                    visible = true;
                }
                break;
            case "-olevel":
                int tmp = Integer.parseInt(options[i + 1]);
                if (tmp > 3 || tmp < 1) {
                    System.out.println("出力モードは1,2,3のいずれかです．その他の値が入力されています．");
                    System.out.println("既定値で実行します．");
                } else {
                    outputLevel = tmp;
                }
                break;

            case "-auto":
                readCommandList(commandList, "resource/command_list/command_list_green.txt");
                if ("true".equals(options[i + 1])) {
                    commandList.addAll(sampleCommandList);
                }

                break;
            case "-test":
                numberOfGames = Integer.parseInt(options[i + 1]);
                isTest = true;
                readCommandList(commandList, "resource/command_list/command_list.txt");
                outputLevel = 0;
                break;
            default:
                throw new ArrayIndexOutOfBoundsException();
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
    }

}