/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package me.camerongray.teamlocker.core;

/**
 *
 * @author camerong
 */
public class LockerCommunicationException extends LockerNonFatalException {
    public LockerCommunicationException(String message) {
        super(message);
    }
    
    public LockerCommunicationException(String message, Exception cause) {
        super(message, cause);
    }

    public LockerCommunicationException(Exception e) {
        super("Unable to communicate with server, check your network "+
                    "connection and ensure that your username/password are correct", e);
    }
}
