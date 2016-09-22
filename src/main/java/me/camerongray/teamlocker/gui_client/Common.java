/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package me.camerongray.teamlocker.gui_client;

import java.awt.Frame;
import javax.swing.JOptionPane;
import me.camerongray.teamlocker.core.LockerNonFatalException;

/**
 *
 * @author camerong
 */
public class Common {
    public static void handleNonFatalException(Frame frame, Throwable e) {
        JOptionPane.showMessageDialog(frame, e.getMessage(), "Error!", JOptionPane.ERROR_MESSAGE);
    }
    
    public static void handleFatalException(Frame frame, Throwable e) {
        (new FatalErrorDialog(frame, e)).setVisible(true);
        System.exit(1);
    }
    
    public static void handleSwingWorkerException(Frame frame, Throwable fullException) {
        Common.handleGeneralException(frame, fullException.getCause(), false);
    }
    
    public static void handleGeneralException(Frame frame, Throwable fullException) {
        Common.handleGeneralException(frame, fullException, false);
    }
    
    public static void handleGeneralException(Frame frame, Throwable fullException, boolean wrapped) {
        Throwable e = (wrapped) ? fullException.getCause() : fullException;
        if (e.getClass().getSuperclass().equals(LockerNonFatalException.class)) {
            Common.handleNonFatalException(frame, e);
        } else {
            Common.handleFatalException(frame, fullException);
        }
    }
}
