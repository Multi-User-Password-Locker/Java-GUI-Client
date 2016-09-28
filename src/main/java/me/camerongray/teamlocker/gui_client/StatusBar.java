/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package me.camerongray.teamlocker.gui_client;

import javax.swing.JLabel;
import javax.swing.JProgressBar;

/**
 *
 * @author camerong
 */
public class StatusBar {
    JLabel label;
    JProgressBar progressBar;

    public StatusBar(JLabel label, JProgressBar progressBar) {
        this.label = label;
        this.progressBar = progressBar;
    }
    
    public void hide() {
        this.setVisibility(false);
    }
    
    public void show() {
        this.setVisibility(true);
    }
    
    private void setVisibility(boolean visible) {
        this.label.setVisible(visible);
        this.progressBar.setVisible(visible);
    }
    
    public void setStatus(String status, boolean indeterminate) {
        this.label.setText(status);
        if(indeterminate) {
            this.progressBar.setIndeterminate(true);
        }
        this.show();
    }
    
    public void setReady() {
        this.label.setText("Ready");
        this.progressBar.setIndeterminate(false);
        this.progressBar.setValue(0);
        this.show();
    }
    
    public void setValue(int value) {
        this.progressBar.setValue(value);
    }
    
    public int getValue() {
        return this.progressBar.getValue();
    }
    
    public void incrementValue(int increment) {
        this.setValue(this.getValue() + increment);
    }
}
