/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package me.camerongray.mupl.core;

import java.util.Base64;

/**
 *
 * @author camerong
 */
public class User {
    private int id;
    private String fullName;
    private String username;
    private String email;
    private String publicKey;
    private byte[] encryptedPrivateKey;
    private boolean admin;
    private byte[] pbkdf2Salt;
    private byte[] aesIv;
    private String authHash;
    
    public User() {}
    
    // <editor-fold defaultstate="collapsed" desc="Getters/Setters">
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPublicKey() {
        return publicKey;
    }

    public void setPublicKey(String publicKey) {
        this.publicKey = publicKey;
    }

    public byte[] getEncryptedPrivateKey() {
        return encryptedPrivateKey;
    }

    public void setEncryptedPrivateKey(byte[] encryptedPrivateKey) {
        this.encryptedPrivateKey = encryptedPrivateKey;
    }
    
    public void setEncryptedPrivateKey(String encryptedPrivateKey) {
        this.encryptedPrivateKey = Base64.getDecoder().decode(encryptedPrivateKey);
    }

    public boolean isAdmin() {
        return admin;
    }

    public void setAdmin(boolean admin) {
        this.admin = admin;
    }

    public byte[] getPbkdf2Salt() {
        return pbkdf2Salt;
    }

    public void setPbkdf2Salt(byte[] pbkdf2Salt) {
        this.pbkdf2Salt = pbkdf2Salt;
    }
    
    public void setPbkdf2Salt(String pbkdf2Salt) {
        this.pbkdf2Salt = Base64.getDecoder().decode(pbkdf2Salt);
    }

    public byte[] getAesIv() {
        return aesIv;
    }

    public void setAesIv(byte[] aesIv) {
        this.aesIv = aesIv;
    }
    
    public void setAesIv(String aesIv) {
        this.aesIv = Base64.getDecoder().decode(aesIv);
    }

    public String getAuthHash() {
        return authHash;
    }

    public void setAuthHash(String authHash) {
        this.authHash = authHash;
    }
    // </editor-fold>
}
