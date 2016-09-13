/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package me.camerongray.teamlocker.gui_client;

import java.awt.Component;
import java.awt.Frame;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import me.camerongray.teamlocker.core.LockerRuntimeException;

/**
 *
 * @author camerong
 */
public class Common {
    public static void handleRuntimeException(Frame frame, LockerRuntimeException e) {
        JOptionPane.showMessageDialog(frame, e.getMessage(), "Error!", JOptionPane.ERROR_MESSAGE);
    }
    
    public static void handleFatalException(Frame frame, Throwable e) {
        (new FatalErrorDialog(frame, e)).setVisible(true);
        System.exit(1);
    }
    
    public static void handleSwingWorkerException(Frame frame, Exception ex) {
        // TODO: Check type of exception rather than rethrowing, currenly flying
        // so no internet connection and can't remember how to do this!
        try {
            throw ex.getCause();
        } catch (LockerRuntimeException e) {
            Common.handleRuntimeException(frame, e);
        } catch (Throwable e) {
            Common.handleFatalException(frame, e);
        }
    }
}
