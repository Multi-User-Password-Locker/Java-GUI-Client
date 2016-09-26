/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package me.camerongray.teamlocker.gui_client;

import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import javax.swing.SwingWorker;
import me.camerongray.teamlocker.core.CryptoException;
import me.camerongray.teamlocker.core.Locker;
import me.camerongray.teamlocker.core.LockerCommunicationException;
import me.camerongray.teamlocker.core.LockerRuntimeException;
import me.camerongray.teamlocker.core.User;

/**
 *
 * @author camerong
 */
public class ChangePassword extends javax.swing.JDialog {
    private Locker locker;
    private User user;
    private MainWindow parent;
    
    /**
     * Creates new form ChangePassword
     */
    public ChangePassword(MainWindow parent, boolean modal, User user) {
        super(parent, modal);
        initComponents();
        this.locker = Locker.getInstance();
        this.user = user;
        this.parent = parent;
        this.getRootPane().setDefaultButton(btnChangePassword);
        this.populateUi();
    }
    
    public void populateUi() {
        this.panelStatus.setVisible(false);
        this.lblUsername.setText(this.user.getUsername());
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        lblUsername = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        txtNewPassword = new javax.swing.JPasswordField();
        jLabel4 = new javax.swing.JLabel();
        txtConfirmNewPassword = new javax.swing.JPasswordField();
        btnCancel = new javax.swing.JButton();
        btnChangePassword = new javax.swing.JButton();
        panelStatus = new javax.swing.JPanel();
        jProgressBar1 = new javax.swing.JProgressBar();
        jLabel5 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Change Password - TeamPass");
        setLocationByPlatform(true);

        jLabel1.setText("Changing password for user: ");

        lblUsername.setText("USERNAME");

        jLabel3.setText("New Password:");

        jLabel4.setText("Confirm New Password:");

        btnCancel.setText("Cancel");
        btnCancel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCancelActionPerformed(evt);
            }
        });

        btnChangePassword.setText("Change Password");
        btnChangePassword.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnChangePasswordActionPerformed(evt);
            }
        });

        jProgressBar1.setIndeterminate(true);

        jLabel5.setText("Updating Password...");

        javax.swing.GroupLayout panelStatusLayout = new javax.swing.GroupLayout(panelStatus);
        panelStatus.setLayout(panelStatusLayout);
        panelStatusLayout.setHorizontalGroup(
            panelStatusLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelStatusLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel5)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jProgressBar1, javax.swing.GroupLayout.PREFERRED_SIZE, 122, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        panelStatusLayout.setVerticalGroup(
            panelStatusLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelStatusLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panelStatusLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel5)
                    .addComponent(jProgressBar1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(btnCancel)
                        .addGap(18, 18, 18)
                        .addComponent(panelStatus, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGap(18, 18, Short.MAX_VALUE)
                        .addComponent(btnChangePassword))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(lblUsername, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel4)
                            .addComponent(jLabel3, javax.swing.GroupLayout.Alignment.TRAILING))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(txtNewPassword)
                            .addComponent(txtConfirmNewPassword))))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(lblUsername))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(txtNewPassword, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel4)
                    .addComponent(txtConfirmNewPassword, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(btnChangePassword)
                        .addComponent(btnCancel))
                    .addComponent(panelStatus, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnCancelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCancelActionPerformed
        this.dispose();
    }//GEN-LAST:event_btnCancelActionPerformed

    private void btnChangePasswordActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnChangePasswordActionPerformed
        if (!Arrays.equals(this.txtNewPassword.getPassword(), this.txtConfirmNewPassword.getPassword())) {
            JOptionPane.showMessageDialog(this, "Passwords do not match!",
                        "Error Changing Password", JOptionPane.ERROR_MESSAGE);
            return;
        }
        this.panelStatus.setVisible(true);
        (new ChangePasswordTask(this, this.locker, this.user, new String(this.txtNewPassword.getPassword()))).execute();
    }//GEN-LAST:event_btnChangePasswordActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnCancel;
    private javax.swing.JButton btnChangePassword;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JProgressBar jProgressBar1;
    private javax.swing.JLabel lblUsername;
    private javax.swing.JPanel panelStatus;
    private javax.swing.JPasswordField txtConfirmNewPassword;
    private javax.swing.JPasswordField txtNewPassword;
    // End of variables declaration//GEN-END:variables
    
    class ChangePasswordTask extends SwingWorker<Void, Object> {
        ChangePassword dialog;
        String newPassword;
        User user;
        Locker locker;

        public ChangePasswordTask(ChangePassword dialog, Locker locker, User user, String newPassword) {
            this.dialog = dialog;
            this.newPassword = newPassword;
            this.user = user;
            this.locker = locker;
        }
        
        @Override
        public Void doInBackground() throws LockerRuntimeException, CryptoException, LockerCommunicationException {
            this.user.changePasswordOnServer(this.newPassword);
            return null;
        }
        
        @Override
        public void done() {
            this.dialog.panelStatus.setVisible(false);
            try {
                get();
            } catch (Exception e) {
                ExceptionHandling.handleSwingWorkerException(parent, e);
                return;
            }
            JOptionPane.showMessageDialog(this.dialog, "Password updated successfully!  You will now be logged out.",
                            "Password Updated", JOptionPane.INFORMATION_MESSAGE);
            // Logout
            this.dialog.dispose();
            this.dialog.parent.dispose();
            new Login().setVisible(true);
        }
    }
}
