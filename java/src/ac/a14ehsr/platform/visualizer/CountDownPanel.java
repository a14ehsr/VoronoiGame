package ac.a14ehsr.platform.visualizer;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;

import javax.swing.JFrame;
import javax.swing.JPanel;

public class CountDownPanel extends JPanel implements Runnable {
    private int size;
    private long remainTime; // milli sec
    private long remainTimeCop;
    private boolean stop;
    private int ringWidth;
    private int radius;
    private double area;
    public CountDownPanel(int size) {
        this.size = size;
        ringWidth = size/5;
        remainTime = 5000;
        remainTimeCop = remainTime;
        radius = size/2;
        stop = false;
    }

    @Override
    public void run() {
        while (!stop && remainTime >= 0) {
            // このフレームで塗ってほしい領域
            //remainTimeCop = (long)(1000*);
            double rad = (1000-remainTime%1000)/1000.0 * Math.PI; // 0~PIに正規化
            area = 0.5*Math.cos(rad) + 0.5;
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

    public void stop() {
        stop = true;
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
        g.drawOval(0 + ringWidth/2, 0 + ringWidth/2, size-ringWidth, size-ringWidth);
        g.drawOval(0, 0, size, size);
        g.setFont(new Font("Arial", Font.BOLD, size/10));
        g.drawString(remainSec + "", radius-size/60, radius+size/60);
        double s = (radius - ringWidth/2);
        for(int i = 0; i < (int)(area*36000); i++) {
            double radian = (i/18000.0) * Math.PI;
            int cx = (int)(radius + s*Math.sin(radian));
            int cy = (int)(radius - s*Math.cos(radian));
            int rx = (int)(radius + radius * Math.sin(radian));
            int ry = (int)(radius - radius * Math.cos(radian));
            //System.err.println(radian + " | " + cx + ":" + cy + " " + rx + ":" +  ry);
            g.drawLine(cx, cy, rx, ry);
        }
        repaint();
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(500,550);
        CountDownPanel cdp = new CountDownPanel(300);
        frame.getContentPane().add(cdp);
        frame.setVisible(true);

        Thread t = new Thread(cdp);
        t.start();
    }
}