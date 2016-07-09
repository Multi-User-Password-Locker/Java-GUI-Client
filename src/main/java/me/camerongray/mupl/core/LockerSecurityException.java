package me.camerongray.mupl.core;

/**
 * Created by camerong on 09/07/16.
 */
public class LockerSecurityException extends Exception {
    public LockerSecurityException(String message) {
        super(message);
    }

    public LockerSecurityException(LockerSecurityException e) {
        super(e);
    }
}
