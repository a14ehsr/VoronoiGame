package ac.a14ehsr.platform.visualizer;

import java.awt.Color;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import java.awt.BorderLayout;
import javax.swing.JComboBox;

import java.util.List;
import java.util.ArrayList;

import ac.a14ehsr.platform.GamePlatform;

public class GameModeWindow implements KeyListener{
    private JFrame frame;
    private JPanel mainMenuPanel;
    private Visualizer gamePanel;

    public GameModeWindow() {
        //VisualizerPanel visualizer = new VisualizerPanel(30, 20);
        // 入力を待ってからスタート
        /*
        try {
            battle(new TronBattle(numberOfPlayers, players, visualizer), 1, true, 3000);
        } catch (IOException e) {
            e.printStackTrace();
        }
        */

        frame = new JFrame("Tron Battle");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setName("Tron Battle");
        frame.setSize(800, 600);
        frame.setVisible(true);

        mainMenuPanel = new MainMenuPanel();

        frame.getContentPane().add(mainMenuPanel);

        frame.validate();

        //frame.addKeyListener(this);
    }

    class MainMenuPanel extends JPanel {
        ButtonGroup numOfPlayerButtonGroup;
        JToggleButton[] numOfPlayerButtons;
        ButtonGroup numOfHumanPlayerButtonGroup;
        JToggleButton[] numOfHumanPlayerButtons;

        JPanel selectPanel;
        //JPanel numOfHumanPlayerButtonPanel;

        JButton startButton;

        JComboBox[] playProgramSelect;
        
        MainMenuPanel() {
            setBackground(Color.BLUE);

            setLayout(new GridLayout(2,1));

            Font font = new Font("", Font.BOLD, 30);
            selectPanel = new JPanel();
            int maxPlayer = 7;
            selectPanel.setLayout(new GridLayout(1,maxPlayer));
            playProgramSelect = new JComboBox[maxPlayer];
            String[] player = {"なし", "人間","legend","mucchin","直進", "時計周り", "ランダム"};
            for(int i = 0; i < maxPlayer; i++) {
                playProgramSelect[i] = new JComboBox<String>(player);
                selectPanel.add(playProgramSelect[i]);
            }
            playProgramSelect[0].setSelectedIndex(1);
            playProgramSelect[1].setSelectedIndex(1);
            playProgramSelect[2].setSelectedIndex(2);
            playProgramSelect[3].setSelectedIndex(2);

            add(selectPanel);

            startButton = new JButton("start");
            add(startButton);

            startButton.addActionListener(new ActionListener(){
                @Override
                public void actionPerformed(ActionEvent e) {
                    setVisible(false);
                    gamePanel = new Visualizer(30,20, frame);
                    JButton exitButton =  new JButton("EXIT");
                    exitButton.addActionListener(new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            gamePanel.setVisible(false);
                            mainMenuPanel.setVisible(true);
                        }
                    });

                    gamePanel.add(exitButton, BorderLayout.EAST);
                    gamePanel.setGMW(true);
                    frame.getContentPane().add(gamePanel);
                    gamePanel.setVisible(true);

                    String[] select = new String[maxPlayer];
                    for(int i = 0; i < maxPlayer; i++) {
                        select[i] = (String)(playProgramSelect[i].getSelectedItem());
                    }

                    List<String> commandList = new ArrayList<>();
                    for(int i = 0; i < maxPlayer; i++) {
                        if("なし".equals(select[i])) {
                            continue;
                        }
                        switch(select[i]) {
                            case "人間":
                                commandList.add("-human");
                                break;
                            case "legend":
                                commandList.add("./ai_programs//P_Python");
                                break;
                            case "mucchin":
                                commandList.add("./ai_programs/P_mucchin");
                                break;
                            case "直進":
                                commandList.add("java -classpath java/src/ ac.a14ehsr.sample_ai.Ai_Straight");
                                break;
                            case "時計周り":
                                commandList.add("java -classpath java/src/ ac.a14ehsr.sample_ai.Ai_Clockwise");
                            break;
                            case "ランダム":
                                commandList.add("java -classpath java/src/ ac.a14ehsr.sample_ai.Ai_Random");
                            break;
                        }
                    }

                    String[] commands = (String[])commandList.toArray(new String[commandList.size()]);
                    Thread th = new Thread(new GamePlatform(commands.length, commands, gamePanel, frame));
                    th.start();
                    frame.validate();
                    gamePanel.validate();
                    frame.requestFocus();

                    
                }
            });
        }

    }

    public static void main(String[] args) {
        new GameModeWindow();
    }

    @Override
    public void keyReleased(KeyEvent e) {
        System.err.println(e.getKeyChar());
    }

    @Override
    public void keyTyped(KeyEvent e) {
        System.err.println(e.getKeyChar());   
    }
    @Override
    public void keyPressed(KeyEvent e) {
        System.err.println("T:"+e.getKeyCode());
    }
}