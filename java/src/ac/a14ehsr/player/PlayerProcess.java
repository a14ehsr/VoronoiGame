package ac.a14ehsr.player;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

import ac.a14ehsr.exception.TimeoutException;
import ac.a14ehsr.exception.TimeoverException;

public class PlayerProcess {
    private Process process;
    private InputStream inputStream;
    private OutputStream outputStream;
    private BufferedReader bufferedReader;
    private ErrorReader errorReader;

    private String name;

    /**
     * 文字列受け取り用の一時変数
     * 受け取ったらnullなどでリセットする
     */
    private String receiveString;


    PlayerProcess(Runtime runtime, String runCommand, int code) throws IOException {
        process = runtime.exec(runCommand);
        outputStream = process.getOutputStream();
        inputStream = process.getInputStream();
        bufferedReader = new BufferedReader(new InputStreamReader(inputStream));

        errorReader = new ErrorReader(process.getErrorStream());
        errorReader.start();

        if (!process.isAlive())
            throw new IOException("次のサブプロセスを起動できませんでした．:" + process);
    }

    /**
     * playerにnumを送る
     * @param num
     * @throws IOException
     */
    void send(int num) throws IOException {
        send(String.valueOf(num));
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
        errorReader.setPlayerName(name);
    }

    /**
     * playerにmesを送る
     * 末尾に改行があると不具合を起こす可能性がある．末尾改行のreplaceを組み込むのも手かもしれない
     * @param mes
     * @throws IOException
     */
    void send(String mes) throws IOException {
        outputStream.write((mes + "\n").getBytes()); // 初期座標を渡す
        outputStream.flush();
    }

    void send(int[] nums) throws IOException {
        for(int i = 0; i < nums.length-1; i++) {
            outputStream.write((nums[i] + " ").getBytes()); // 初期座標を渡す
        }
        outputStream.write((nums[nums.length-1] + "\n").getBytes()); // 初期座標を渡す
        outputStream.flush();
    }

    String receiveMes(long timeout, long timelimit) throws IOException, TimeoutException, TimeoverException {
        if (!process.isAlive())
            throw new IOException("値取得時に次のプレイヤーのサブプロセスが停止しました :" + name);
        Thread thread = new ReceiveThread();
        thread.start();
        long start = System.nanoTime();
        try {
            thread.join(timeout);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        long calculateTime = System.nanoTime() - start;
        // TODO: tyimoutしたプレイヤーをkillしてむりやりすすめる
        if (receiveString == null)
            throw new TimeoutException("一定時間以内に次のプレイヤーから値を取得できませんでした :" + name);

        if(calculateTime/1.0e6 > timelimit) {
            throw new TimeoverException("制限時間内に次のプレイヤーから値を取得できませんでした :" + name);
        }

        return receiveString;
    }

    int receiveNum(long timeout, long timelimit) throws IOException, TimeoutException, TimeoverException, NumberFormatException {
        String tmp = receiveMes(timeout, timelimit);
        return Integer.parseInt(tmp);
    }

    /**
     * 数値の取得用Thread
     */
    class ReceiveThread extends Thread {
        public void run() {
            try {
                receiveString = bufferedReader.readLine();
            } catch (IOException e) {
                e.printStackTrace();
                // outputException = e;
            }
        }
    }


    /**
     * サブプロセスを終了
     */
    void destroy() {
        if (process == null) return;
        try {
            process.destroy();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}