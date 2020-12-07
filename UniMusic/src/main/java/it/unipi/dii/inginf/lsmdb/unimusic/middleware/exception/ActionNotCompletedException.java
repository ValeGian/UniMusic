package it.unipi.dii.inginf.lsmdb.unimusic.middleware.exception;

public class ActionNotCompletedException extends Exception{
    private static final long serialVersionUID = 7718828512143293558L;

    private int code;

    public ActionNotCompletedException(int code) {
        super();
        this.code = code;
    }

    public ActionNotCompletedException(String message, Throwable cause, int code) {
        super(message, cause);
        this.code = code;
    }

    public ActionNotCompletedException(String message, int code) {
        super(message);
        this.code = code;
    }

    public ActionNotCompletedException(Throwable cause, int code) {
        super(cause);
        this.code = code;
    }

    public int getCode() {
        return this.code;
    }

    public ActionNotCompletedException(String message) {
        super(message);
    }

    public ActionNotCompletedException(Throwable throwable) {
        super(throwable);
    }

    public ActionNotCompletedException(String message, Throwable throwable) {
        super(message, throwable);
    }

}
