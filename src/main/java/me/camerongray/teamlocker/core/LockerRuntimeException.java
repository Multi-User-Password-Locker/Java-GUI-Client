package me.camerongray.teamlocker.core;

/**
 * Created by camerong on 09/07/16.
 */
public class LockerRuntimeException extends LockerNonFatalException {
    public LockerRuntimeException(String message) {
        super(message);
    }

    public LockerRuntimeException(Exception e) {
        super(e);
    }
}
