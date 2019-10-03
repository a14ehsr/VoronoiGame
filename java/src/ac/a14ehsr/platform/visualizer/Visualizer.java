package ac.a14ehsr.platform.visualizer;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.LineBorder;

public class Visualizer extends JPanel {
    private JFrame frame;
    private JPanel mainPanel;
    private JPanel namePanel;
    private JPanel[][] panels;

    private JLabel[] nameLabels;

    private boolean isGMW;


    CountDownBarPanel cdp;
    Thread th;
    JFrame cdf;

    private int height;
    private int width;

    private static final Color[] playerColor = {Color.RED, Color.BLUE, Color.GREEN, Color.YELLOW, Color.PINK, Color.ORANGE};
    private static final Color notAchieve = Color.LIGHT_GRAY;


    private static JFrame createWindow(int width, int height) {
        JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
        frame.setSize(width * 40,height * 40);
        
        return frame;
    }

    /**
     * @param isGMW the isGMW to set
     */
    public void setGMW(boolean isGMW) {
        this.isGMW = isGMW;
    }

    public Visualizer(int width, int height) {
        this(width, height, createWindow(width, height));
        frame.getContentPane().add(this, BorderLayout.CENTER);
    }

    public Visualizer(int width, int height, JFrame frame) {
        super();
        this.width = width;
        this.height = height;

        this.frame = frame;
        isGMW = false;
        mainPanel = new JPanel();
        mainPanel.setLayout(new GridLayout(height + 2,width + 2));
        
        panels = new JPanel[height + 2][width + 2];
        LineBorder border = new LineBorder(Color.BLACK);

        for(int y = 0; y < panels.length; y++) {
            for(int x = 0; x < panels[y].length; x++) {
                panels[y][x] = new JPanel();
                panels[y][x].setBackground(notAchieve);
                panels[y][x].setBorder(border);
                mainPanel.add(panels[y][x]);
            }
        }
        for(int x = 0; x < panels[0].length; x++) {
            panels[0][x].setBackground(Color.GRAY);
            panels[height+1][x].setBackground(Color.GRAY);
        }

        for(int y = 0; y < panels.length; y++) {
            panels[y][0].setBackground(Color.GRAY);
            panels[y][width + 1].setBackground(Color.GRAY);
        }

        
        namePanel = new JPanel();
        namePanel.setBackground(Color.BLACK);
        this.setLayout(new BorderLayout());
        this.add(mainPanel,BorderLayout.CENTER);
        this.add(namePanel,BorderLayout.NORTH);
        this.add(new JPanel(), BorderLayout.SOUTH);
        frame.validate();
    }

    public void setName(String[] names) {
        nameLabels = new JLabel[names.length];
        for(int p = 0; p < names.length; p++) {
            nameLabels[p] = new JLabel(names[p]);
            nameLabels[p].setForeground(playerColor[p]);
            namePanel.add(nameLabels[p]);
        }
        frame.validate();
    }

    public void setNameBorder(int player, Color color) {
        nameLabels[player].setBorder(new LineBorder(color));
    }

    public void setNameColor(int player, Color color) {
        nameLabels[player].setForeground(color);
    }

    public void resetNameColor() {
        for(int p = 0; p < nameLabels.length; p++) {
            nameLabels[p].setForeground(playerColor[p]);
        }
    }

    public void setBorder(int x, int y, Color color, int size) {
        //System.err.println(x+"," + y + "の色を"+color+"に変更しました");
        if(x==-1) return;
        panels[y][x].setBorder(new LineBorder(color, size));
    }

    public void setColor(int player, int x, int y) {
        if(cdp != null) {
            cdp.stop();
            this.remove(cdp);
        }
        panels[y][x].setBackground(playerColor[player]);
        //panels[y][x].setBorder(new LineBorder(Color.WHITE, 3));
        panels[y][x].repaint();
        if(isGMW) {

            cdp = new CountDownBarPanel(getWidth(),200);
            this.add(cdp, BorderLayout.SOUTH);   
            (th = new Thread(cdp)).start();
            cdp.validate();
            cdp.repaint();
        }

        //mainPanel.validate();
        //this.validate();
        //this.repaint();
        frame.validate();
    }

    public void relese(int player, int x, int y) {
        panels[y][x].setBackground(notAchieve);
        //setBorder(x, y, Color.BLACK, 1);
        panels[y][x].repaint();
        mainPanel.validate();
    }

    public void dispose() {
        if(frame == null) return;
        if(isGMW) return;
        frame.dispose();
    }

    public void reset() {
        for(int y = 1; y <= height; y++) {
            for(int x = 1; x <= width; x++) {
                panels[y][x].setBackground(notAchieve);
                setBorder(x, y, Color.BLACK, 1);
            }
        }       
    }

    public void validate() {
        for(int y = 0; y < panels.length; y++) {
            for(int x = 0; x < panels[y].length; x++) {
                panels[y][x].repaint();
            }
        }
        mainPanel.repaint();
        mainPanel.validate();
    }
}