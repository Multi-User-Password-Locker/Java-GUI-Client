/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package me.camerongray.mupl.core;
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
    
    public Account(int id, byte[] encryptedMetadata, byte[] encryptedAesKey, byte[] privateKey) throws CryptoException {
        String aesKeyJson = new String(Crypto.rsaDecryptWithPrivateKey(privateKey, encryptedAesKey));
        JSONObject aesKeyParts = new JSONObject(aesKeyJson);
        
        String metadataJson = new String(Crypto.aesDecrypt(aesKeyParts.getString("key"), aesKeyParts.getString("iv"), encryptedMetadata));
        
        JSONObject metadata = new JSONObject(metadataJson);
        this.id = id;
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
