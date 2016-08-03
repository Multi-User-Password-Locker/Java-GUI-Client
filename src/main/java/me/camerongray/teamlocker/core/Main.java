package me.camerongray.teamlocker.core;

/**
 * Created by camerong on 09/07/16.
 */
public class Main {
    public static void main(String[] args) {
        try {
            Locker l = new Locker("locahost", 5000, "camerongray", "password");
            System.out.println(l.checkAuth());
        } catch (LockerRuntimeException e) {
            System.out.println(e.getMessage());
        }
    }
}
