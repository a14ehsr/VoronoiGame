package ac.a14ehsr.player;

import java.io.InputStream;

/**
 * エラー出力のReader
 */
class ErrorReader extends Thread {
    InputStream error;
    String playerName;
    public ErrorReader(InputStream is) {
        error = is;
    }

    /**
     * @param playerName the playerName to set
     */
    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }

    public void run() {
        try {
            byte[] ch = new byte[50000];
            int read;
            while ((read = error.read(ch)) > 0) {
                String s = new String(ch, 0, read);
                //System.out.print(playerName+"'s error: " + s);
                System.out.print(s);
                System.out.flush();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}