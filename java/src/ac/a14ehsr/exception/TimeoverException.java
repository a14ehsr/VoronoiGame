package ac.a14ehsr.exception;
/**
 * タイムオーバー発生時に投げる例外クラス
 */
public class TimeoverException extends Exception {
    /**
     * コンストラクタ
     * 
     * @param mes メッセージ
     */
    public TimeoverException(String mes) {
        super(mes);
    }
}