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
public class Folder {
    private int id;
    private String name;
    private boolean read;
    private boolean write;

    public Folder(int id, String name, boolean read, boolean write) {
        this.id = id;
        this.name = name;
        this.read = read;
        this.write = write;
    }
    
    public Folder(int id, String name) {
        this.id = id;
        this.name = name;

    }
    
    public Folder() {}

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public String toString() {
        return this.name;
    }
}
