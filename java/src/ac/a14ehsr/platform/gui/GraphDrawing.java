package ac.a14ehsr.platform.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridLayout;

import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.LineBorder;

public class GraphDrawing {
    private JFrame frame;
    private JPanel panel;
    private JPanel centerPanel;
    private JPanel header;
    private JLabel[][] nodeLabels;
    private Color[] colors;
    private String[] colorNames = { "R", "G", "B", "Y" };

    public GraphDrawing(int n, int m, int[] weight, String[] names) {
        frame = new JFrame("Voronoi Game");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(500, 600);
        frame.setVisible(true);
        colors = new Color[4];
        colors[0] = Color.RED;
        colors[1] = Color.GREEN;
        colors[2] = Color.BLUE;
        colors[3] = Color.YELLOW;
        panel = new JPanel();
        // panel.setLayout(new GridLayout(2, 1));
        panel.setLayout(new BorderLayout());

        centerPanel = new JPanel();
        // frame.getContentPane().setLayout(new BorderLayout());
        frame.getContentPane().add(panel);

        header = new JPanel();
        // header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        panel.add(header, BorderLayout.NORTH);
        panel.add(centerPanel, BorderLayout.CENTER);

        // panel.add(header, BorderLayout.SOUTH);
        Font font = new Font("ＭＳ ゴシック", Font.BOLD, 20);
        frame.getContentPane().add(panel, BorderLayout.CENTER);
        centerPanel.setLayout(new GridLayout(n, m));
        JLabel[] nameLabels = new JLabel[names.length];
        for (int i = 0; i < nameLabels.length; i++) {
            nameLabels[i] = new JLabel(String.format("%s:%-10s", colorNames[i], names[i]));
            nameLabels[i].setHorizontalAlignment(JLabel.LEFT);
            nameLabels[i].setFont(font);
            header.add(nameLabels[i]);
        }
        nodeLabels = new JLabel[n][m];

        LineBorder border = new LineBorder(Color.BLACK, 2, true);
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < m; j++) {
                nodeLabels[i][j] = new JLabel(weight[i * m + j] + "");
                nodeLabels[i][j].setHorizontalAlignment(JLabel.CENTER);
                nodeLabels[i][j].setVerticalAlignment(JLabel.CENTER);
                nodeLabels[i][j].setFont(font);
                nodeLabels[i][j].setBorder(border);
                nodeLabels[i][j].setOpaque(true);
                centerPanel.add(nodeLabels[i][j]);
            }
        }

        frame.validate();
    }

    public void dispose() {
        frame.dispose();
    }

    public void setColor(int row, int col, int color) {
        nodeLabels[row][col].setBackground(colors[color]);
        frame.validate();
    }

    public void setColor(int[][] color) {
        for (int i = 0; i < color.length; i++) {
            for (int j = 0; j < color[i].length; j++) {
                if (color[i][j] != -1) {
                    nodeLabels[i][j].setBackground(colors[color[i][j]]);
                }
            }
        }
        panel.repaint();
        frame.validate();
    }
}