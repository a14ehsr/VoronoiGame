package ac.a14ehsr.platform;

import java.io.PrintWriter;
import java.util.Scanner;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.io.File;

/**
 * プラットフォームのコマンドライン引数を受け取って諸々の処理をするクラス
 * パラメータは resource/setting/setting.txtから読み取る.
 * 引数無し実行時には対話モードで動作する．
 */
class Setting {
    private List<String> attackCommand;
    private List<String> defenceCommand;
    private List<String> sampleAttackCommand;
    private List<String> sampleDefenceCommand;
    private int gameNum;
    private int roundNum;
    private int outputLevel;
    private boolean isTest;

    private int minNumber;
    private int maxNumber;
    private int change;
    /**
     * デフォルトコンストラクタ
     * コマンドのリストの準備と設定ファイル読み込み
     */
    Setting() {
        isTest = false;
        attackCommand = new ArrayList<>();
        defenceCommand = new ArrayList<>();
        sampleAttackCommand = new ArrayList<>();
        sampleDefenceCommand = new ArrayList<>();

        try {
            defaultSetting();  
        } catch (Exception e) {
            System.err.println("settingファイルの様式が規定通りになっていません．");
            System.err.println("起動を中止します．");
            e.printStackTrace();
            System.exit(0);
        }
        
        String common = "java -classpath java/src/ ac.a14ehsr.sample_ai.";
        sampleAttackCommand.add(common+"attack.A_SameAsk");
        sampleAttackCommand.add(common + "attack.A_RandomAsk");
        sampleDefenceCommand.add(common+"defence.D_SameDeclare");
        sampleDefenceCommand.add(common + "defence.D_RandomDeclare");
    }

    List<String> getAttackCommand() {
        return attackCommand;
    }

    List<String> getDefenceCommand() {
        return defenceCommand;
    }

    List<String> getSampleAttackCommand() {
        return sampleAttackCommand;
    }

    List<String> getSampleDefenceCommand() {
        return sampleDefenceCommand;
    }
    
    /**
     * ゲーム数のgetter
     */
    int getGameNum() {
        return gameNum;
    }

    /**
     * 最大ラウンド数のgetter
     */
    int getRoundNum() {
        return roundNum;
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

    int getMin() {
        return minNumber;
    }

    int getMax() {
        return maxNumber;
    }

    int getChange() {
        return change;
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
        if(!"game".equals(line[0])){
            throw new Exception("line1 need be game");
        }
        gameNum =  Integer.parseInt(line[1]);

        line = sc.nextLine().split(" ");
        if (!"round".equals(line[0])) {
            throw new Exception("line2 need be round");
        }
        roundNum = Integer.parseInt(line[1]);

        line = sc.nextLine().split(" ");
        if (!"outputlevel".equals(line[0])) {
            throw new Exception("line3 need be outputlevel");
        }
        outputLevel = Integer.parseInt(line[1]);

        line = sc.nextLine().split(" ");
        if (!"min".equals(line[0])) {
            throw new Exception("line4 need be min");
        }
        minNumber = Integer.parseInt(line[1]);

        line = sc.nextLine().split(" ");
        if (!"max".equals(line[0])) {
            throw new Exception("line5 need be max");
        }
        maxNumber = Integer.parseInt(line[1]);

        line = sc.nextLine().split(" ");
        if (!"change".equals(line[0])) {
            throw new Exception("line6 need be change");
        }
        change = Integer.parseInt(line[1]);
    }

    /**
     * 設定処理を管理
     * 
     * @param args コマンドライン引数
     */
    void start(final String[] args) {
        boolean successed = false;
        if (args.length > 0) {
            try {
                setOption(args);
            } catch (NumberFormatException e) {
                System.err.println(e);
                System.out.println("オプションがおかしいです．");
                System.out.println("対話モードで起動します．");
                dialogueMode();
            } catch (ArrayIndexOutOfBoundsException e) {
                System.err.println(e);
                System.out.println("オプションがおかしいです．");
                System.out.println("対話モードで起動します．");
                dialogueMode();
            }
        } else {
            dialogueMode();
        }
        System.out.println("設定終了");
    }
    
    /**
     *  コマンドライン引数を元に各設定を行う
     * 
     * @param options コマンドライン引数
     * @return 成功か否か
     * @throws Exception 範囲外アクセス，型変換例外
     */
    void setOption(final String[] options) throws  NumberFormatException, ArrayIndexOutOfBoundsException{
        for (int i = 0; i < options.length; i+=2) {
            switch (options[i]) {
            case "-a":
                attackCommand.add(options[i + 1]);
                break;

            case "-as":
                attackCommand.add(options[i + 1]);
                defenceCommand.addAll(sampleDefenceCommand);
                break;

            case "-ds":
                defenceCommand.add(options[i + 1]);
                attackCommand.addAll(sampleAttackCommand);
                break;

            case "-d":
                defenceCommand.add(options[i + 1]);
                break;

            case "-game":
                gameNum = Integer.parseInt(options[i + 1]);
                break;

            case "-olevel":
                int tmp = Integer.parseInt(options[i + 1]);
                if (tmp > 3 || tmp < 1) {
                    System.out.println("出力モードは1,2,3のいずれかです．その他の値が入力されています．");
                    System.out.println("既定値で実行します．");
                }else{
                    outputLevel = tmp;
                }
                break;

            case "-c":
                change = Integer.parseInt(options[i + 1]);
                break;
            
            case "-min":
                minNumber = Integer.parseInt(options[i + 1]);
                break;

            case "-max":
                maxNumber = Integer.parseInt(options[i + 1]);
                break;

            case "-auto":
                readCommandList(attackCommand, "resource/command_list/attack/attack_command_list_green.txt");
                readCommandList(defenceCommand, "resource/command_list/defence/defence_command_list_green.txt");
                /*
                // サンプルはとりあえずなし
                if("true".equals(options[i + 1])){
                    command.addAll(sampleCommand);
                }
                */
                
                break;
            case "-test":
                gameNum = Integer.parseInt(options[i + 1]);
                isTest = true;
                readCommandList(attackCommand,
                        "resource/command_list/attack/attack_command_list.txt");
                readCommandList(defenceCommand,
                        "resource/command_list/defence/defence_command_list.txt");
                outputLevel = 0;
                break;
            default:
                throw new ArrayIndexOutOfBoundsException();
            }
        }
    }

    /**
     *  対話モードでの設定処理
     */
    void dialogueMode() {
        Scanner sc = new Scanner(System.in);
        String ai;
        System.out.println("対話モード起動");

        // aiの設定
        System.out.println("攻撃プレイヤーを設定します．");
        attackCommand.add(sc.next());
        System.out.println("防御プレイヤーを設定します．");
        defenceCommand.add(sc.next());

        // gameNum 変更処理
        System.out.print("ゲーム数を変更しますか？ (y/n) :");
        if ("y".equals(sc.next())) {
            System.out.print("ゲーム数を入力してください: ");
            gameNum = sc.nextInt();
        }

        System.out.println("対戦を開始します．");
    }

    void readCommandList(List<String> commandList, String fileName) {
        Scanner sc = null;
        try {
            sc = new Scanner(new File(fileName));
        } catch (Exception e) {
            e.printStackTrace();
        }
        while(sc.hasNext()) {
            commandList.add(sc.nextLine());
        }
    }

}