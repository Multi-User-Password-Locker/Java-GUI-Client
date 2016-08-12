/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package me.camerongray.teamlocker.core;

import java.util.Arrays;

/**
 *
 * @author camerong
 */
public class Validation {
    public static void ensureNonEmpty(String value, String inputName) throws LockerRuntimeException {
        if (value.isEmpty()) {
            throw new LockerRuntimeException(inputName + " cannot be empty!");
        }
    }
    
    public static void validatePassword(byte[] password, byte[] passwordConfirmation) throws LockerRuntimeException {
        if (password.length == 0) {
            throw new LockerRuntimeException("You must specify a password!");
        }
        
        if (!Arrays.equals(password, passwordConfirmation)) {
            throw new LockerRuntimeException("Passwords do not match!");
        }
    }
}
