package ac.a14ehsr.exception;
/**
 * タイムアウト発生時に投げる例外クラス
 */
public class TimeoutException extends Exception {
    /**
     * コンストラクタ
     * 
     * @param mes メッセージ
     */
    public TimeoutException(String mes) {
        super(mes);
    }
}