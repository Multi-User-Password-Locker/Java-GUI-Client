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
public class FolderPermission {
    private User user;
    private Folder folder;
    private boolean read;
    private boolean write;

    public FolderPermission(User user, Folder folder, boolean read, boolean write) {
        this.user = user;
        this.folder = folder;
        this.read = read;
        this.write = write;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Folder getFolder() {
        return folder;
    }

    public void setFolder(Folder folder) {
        this.folder = folder;
    }

    public boolean isRead() {
        return read;
    }

    public void setRead(boolean read) {
        this.read = read;
    }

    public boolean isWrite() {
        return write;
    }

    public void setWrite(boolean write) {
        this.write = write;
    }
    
    
}
