/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package me.camerongray.teamlocker.core;

import java.io.IOException;

/**
 *
 * @author camerong
 */
public class CurrentUser {
    private static User instance = null;
    
    protected CurrentUser() {
        // Prevent instantiation
    }
    
    public static User getInstance() throws LockerRuntimeException {
        if (instance == null) {
            throw new LockerRuntimeException("CurrentUser not initialised");
        }
        return instance;
    }
    
    public static void init() throws LockerRuntimeException, IOException, LockerCommunicationException, CryptoException, LockerSimpleException {
        instance = User.getCurrentFromServer();
    }
}
