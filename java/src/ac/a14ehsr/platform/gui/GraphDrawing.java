import java.awt.Color;
import java.awt.Font;
import java.awt.GridLayout;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.LineBorder;

public class GraphDrawing {
    private JFrame frame;
    private JPanel panel;
    private JLabel[][] nodeLabels;
    private Color[] colors;

    public GraphDrawing(int n, int m, int[] weight) {
        frame = new JFrame("Voronoi Game");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(500, 500);
        frame.setVisible(true);

        panel = new JPanel();
        frame.getContentPane().add(panel);

        panel.setLayout(new GridLayout(n, m));

        nodeLabels = new JLabel[n][m];
        Font font = new Font("ＭＳ ゴシック", Font.BOLD, 20);
        LineBorder border = new LineBorder(Color.BLACK, 2, true);
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < m; j++) {
                nodeLabels[i][j] = new JLabel(weight[i * m + j] + "");
                nodeLabels[i][j].setHorizontalAlignment(JLabel.CENTER);
                nodeLabels[i][j].setVerticalAlignment(JLabel.CENTER);
                nodeLabels[i][j].setFont(font);
                nodeLabels[i][j].setBorder(border);
                nodeLabels[i][j].setOpaque(true);
                panel.add(nodeLabels[i][j]);
            }
        }
        colors = new Color[4];
        colors[0] = Color.RED;
        colors[1] = Color.GREEN;
        colors[2] = Color.BLUE;
        colors[3] = Color.YELLOW;
        frame.validate();
    }

    public void setColor(int row, int col, int color) {
        nodeLabels[row][col].setBackground(colors[color]);
        frame.validate();
    }

    public static void main(String[] args) {
        int[] weight = new int[9];
        for (int i = 0; i < 9; i++) {
            weight[i] = i;
        }
        GraphDrawing obj = new GraphDrawing(3, 3, weight);

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                try {
                    Thread.sleep(1000);
                } catch (Exception e) {

                }
                obj.setColor(i, j, i);
            }

        }
    }
}