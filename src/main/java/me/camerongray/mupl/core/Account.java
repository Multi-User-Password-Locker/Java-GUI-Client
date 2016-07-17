/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package me.camerongray.mupl.core;

import java.security.Key;
import java.security.KeyFactory;
import java.security.Security;
import java.security.spec.KeySpec;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.json.JSONObject;

/**
 *
 * @author camerong
 */
public class Account {
    private int id;
    private String name;
    private String username;
    private String notes;
    private String password;
    
    public Account(byte[] encryptedMetadata, byte[] encryptedAesKey, byte[] privateKey) throws Exception {
        Security.addProvider(new BouncyCastleProvider());
        Cipher rsaCipher = Cipher.getInstance("RSA/NONE/OAEPWithSHA1AndMGF1Padding", "BC");
        PKCS8EncodedKeySpec privateKeySpec = new PKCS8EncodedKeySpec(privateKey);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        Key rsaKey = kf.generatePrivate(privateKeySpec);
        rsaCipher.init(Cipher.DECRYPT_MODE, rsaKey);
        
        String aesKeyJson = new String(rsaCipher.doFinal(encryptedAesKey));
        JSONObject aesKeyParts = new JSONObject(aesKeyJson);
        
        SecretKey aesKey = new SecretKeySpec(Base64.getDecoder().decode(aesKeyParts.getString("key")), "AES");
        Cipher dcipher = Cipher.getInstance("AES/CFB8/NoPadding"); // CFB8 makes this byte oriented
        dcipher.init(Cipher.DECRYPT_MODE, aesKey, new IvParameterSpec(Base64.getDecoder().decode(aesKeyParts.getString("iv"))));
        String metadataJson = new String(dcipher.doFinal(encryptedMetadata));
        
        JSONObject metadata = new JSONObject(metadataJson);
        this.name = metadata.getString("name");
        this.username = metadata.getString("username");
        this.notes = metadata.getString("notes");
    }

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

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
    
}
