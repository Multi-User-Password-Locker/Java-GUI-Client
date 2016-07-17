/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package me.camerongray.mupl.core;

/**
 *
 * @author camerong
 */
public class CryptoException extends Exception {
    public CryptoException(String message) {
        super(message);
    }

    public CryptoException(Exception e) {
        super(e);
    }
}
