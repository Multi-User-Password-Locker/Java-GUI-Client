package me.camerongray.mupl.core;

/**
 * Created by camerong on 09/07/16.
 */
public class Main {
    public static void main(String[] args) {
        try {
            Locker l = new Locker("locahost", 5000, "camerongray", "password");
            System.out.println(l.check_auth());
        } catch (LockerRuntimeException e) {
            System.out.println(e.getMessage());
        }
    }
}
