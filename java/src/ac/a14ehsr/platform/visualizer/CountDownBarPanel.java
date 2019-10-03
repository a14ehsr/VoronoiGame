package ac.a14ehsr.platform.visualizer;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;

import javax.swing.JFrame;
import javax.swing.JPanel;

public class CountDownBarPanel extends JPanel implements Runnable {
    private int width, height;
    private long remainTime; // milli sec
    private long countTime;
    private boolean stop;
    //private int ringWidth;
    //private int radius;
    private double area;
    public CountDownBarPanel(int width, int height) {
        this.width = width;
        this.height = height;
        countTime = 5000;
        remainTime = countTime;
    }

    @Override
    public void run() {
        while (!stop && remainTime >= 0) {
            // このフレームで塗ってほしい領域
            area = remainTime / (double)countTime;
            repaint();
            validate();
            try {
                Thread.sleep(10);
            }catch(InterruptedException e) {
                e.printStackTrace();
            }
            remainTime -= 10;
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        int remainSec = (int)(remainTime/1000);
        if(remainSec >= 2) {
            g.setColor(new Color(0,144,0));
        } else if(remainSec >= 1) {
            g.setColor(Color.ORANGE);
        } else {
            g.setColor(Color.RED);
        }
        //g.setFont(new Font("Arial", Font.BOLD, size/10));
        //g.drawString(remainSec + "", radius-size/60, radius+size/60);
        for(int i = 0; i < width * area; i++) {
            g.drawLine(i,0,i,height);
        }
        repaint();
    }

    public void stop() {
        stop = true;
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(500,550);
        CountDownBarPanel cdp = new CountDownBarPanel(300,100);
        frame.getContentPane().add(cdp, BorderLayout.SOUTH);
        frame.setVisible(true);

        Thread t = new Thread(cdp);
        t.start();
    }
}