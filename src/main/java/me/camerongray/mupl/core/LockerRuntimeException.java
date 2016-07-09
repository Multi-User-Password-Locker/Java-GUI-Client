package me.camerongray.mupl.core;

/**
 * Created by camerong on 09/07/16.
 */
public class LockerRuntimeException extends Exception {
    public LockerRuntimeException(String message) {
        super(message);
    }

    public LockerRuntimeException(Exception e) {
        super(e);
    }
}
