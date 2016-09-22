package me.camerongray.teamlocker.core;

/**
 * Created by camerong on 09/07/16.
 */
public class LockerSecurityException extends LockerNonFatalException {
    public LockerSecurityException(String message) {
        super(message);
    }

    public LockerSecurityException(LockerSecurityException e) {
        super(e);
    }
}
