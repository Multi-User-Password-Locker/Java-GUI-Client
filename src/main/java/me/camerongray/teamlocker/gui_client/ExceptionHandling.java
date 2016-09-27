/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package me.camerongray.teamlocker.gui_client;

import java.awt.Frame;
import javax.swing.JOptionPane;
import me.camerongray.teamlocker.core.LockerNonFatalException;
import me.camerongray.teamlocker.core.LockerRemoteException;

/**
 *
 * @author camerong
 */
public class ExceptionHandling {
    public static void showExceptionMessage(Frame frame, Throwable e, boolean exitOnClose) {
        // Do not show details button for exceptions returned from the server,
        // stack trace/details.etc are pointless in this case
        Object[] options;
        if (e instanceof LockerRemoteException) {
            options = new Object[]{"OK"};
        } else {
            options = new Object[]{"OK", "Details"};
        }
        
        final int OK_OPTION = 0;
        final int DETAILS_OPTION = 1;
        
        int result = -1;
        while (result != OK_OPTION) {
            StringBuilder message = new StringBuilder();
            message.append(e.getMessage());
            if (exitOnClose) {
                message.append("\n\nThis is a fatal error, application will quit when OK is pressed.");
            }
            result = JOptionPane.showOptionDialog(frame, message.toString(), "Error!", JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE, null, options, options[OK_OPTION]);
            if (result == DETAILS_OPTION) {
                Throwable ex = (e.getCause() != null) ? e.getCause() : e;
                (new ErrorDetailsDialog(frame, ex)).setVisible(true);
            }
        }
        
        if (exitOnClose) {
            System.exit(1);
        }
    }
    
    public static void handleSwingWorkerException(Frame frame, Throwable fullException) {
        ExceptionHandling.handleException(frame, fullException.getCause(), false);
    }
    
    public static void handleException(Frame frame, Throwable fullException) {
        ExceptionHandling.handleException(frame, fullException, false);
    }
    
    public static void handleException(Frame frame, Throwable fullException, boolean wrapped) {
        Throwable e = (wrapped) ? fullException.getCause() : fullException;
        if (e.getClass().getSuperclass().equals(LockerNonFatalException.class)) {
            ExceptionHandling.showExceptionMessage(frame, e, false);
        } else {
            ExceptionHandling.showExceptionMessage(frame, fullException, true);
        }
    }
}
