/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package me.camerongray.mupl.gui_client;

import javax.swing.UIManager;

/**
 *
 * @author camerong
 */
public class Main {
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            System.out.println("Could not find look and feel, falling back to default");
        }
        new Login(new javax.swing.JFrame(), true).setVisible(true);
    }
}
