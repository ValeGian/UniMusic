package it.unipi.dii.inginf.lsmdb.unimusic.middleware.exception;

public class ActionNotCompletedException extends Exception{

    public ActionNotCompletedException() {
        super("Error: Action not completed");
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
