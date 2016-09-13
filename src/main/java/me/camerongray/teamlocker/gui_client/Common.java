/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package me.camerongray.teamlocker.gui_client;

import java.awt.Component;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import me.camerongray.teamlocker.core.LockerRuntimeException;

/**
 *
 * @author camerong
 */
public class Common {
    public static void handleRuntimeException(Component frame, LockerRuntimeException e) {
        JOptionPane.showMessageDialog(frame, e.getMessage(), "Error!", JOptionPane.ERROR_MESSAGE);
    }
    
    public static void handleFatalException(Component frame, Exception e) {
        JOptionPane.showMessageDialog(frame, e.getMessage(), "Fatal Error!", JOptionPane.ERROR_MESSAGE);
        e.printStackTrace();
        System.exit(1);
    }
    
    public static void handleSwingWorkerException(Component frame, Exception ex) {
        // TODO: Check type of exception rather than rethrowing, currenly flying
        // so no internet connection and can't remember how to do this!
        try {
            throw ex.getCause();
        } catch (LockerRuntimeException e) {
            Common.handleRuntimeException(frame, e);
        } catch (Throwable e) {
            Common.handleFatalException(frame, ex);
        }
    }
}
