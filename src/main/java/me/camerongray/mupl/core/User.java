/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package me.camerongray.mupl.core;

import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.Base64;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

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
    private byte[] privateKey;
    
    public User() {}
    
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

    public byte[] getPrivateKey() {
        return privateKey;
    }

    public void setPrivateKey(byte[] privateKey) {
        this.privateKey = privateKey;
    }
    
    public void decryptPrivateKey(String password) throws Exception {
        final int ITERATION_COUNT = 1000;
        final int KEY_LENGTH = 256;
        SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
        KeySpec spec = new PBEKeySpec(password.toCharArray(), this.pbkdf2Salt, ITERATION_COUNT, KEY_LENGTH);
        SecretKey key = new SecretKeySpec(skf.generateSecret(spec).getEncoded(), "AES");
        Cipher dcipher = Cipher.getInstance("AES/CFB8/NoPadding"); // CFB8 makes this byte oriented
        dcipher.init(Cipher.DECRYPT_MODE, key, new IvParameterSpec(this.aesIv));
        this.privateKey = dcipher.doFinal(this.encryptedPrivateKey);
//        String pk = new String(dcipher.doFinal(this.encryptedPrivateKey), "UTF-8");
//        this.privateKey = pk.substring(0, pk.lastIndexOf("\n")).substring(pk.indexOf("\n")+1).replace("\n", "");
    }
}
