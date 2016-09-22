package me.camerongray.teamlocker.core;

/**
 * Created by camerong on 09/07/16.
 */
public class LockerNonFatalException extends Exception {
    public LockerNonFatalException(String message) {
        super(message);
    }

    public LockerNonFatalException(Exception e) {
        super(e);
    }
    
    public LockerNonFatalException(String message, Exception cause) {
        super(message, cause);
    }
}
