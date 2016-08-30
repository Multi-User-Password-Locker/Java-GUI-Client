/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package me.camerongray.teamlocker.core;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.mashape.unirest.http.Unirest;
import java.security.KeyPair;
import java.util.Base64;
import org.json.JSONObject;

/**
 *
 * @author camerong
 */
public class User {
    private int id;
    private String fullName;
    private String username;
    private String email;
    private byte[] publicKey;
    private byte[] encryptedPrivateKey;
    private boolean admin;
    private byte[] pbkdf2Salt;
    private byte[] aesIv;
    private String authHash;
    private byte[] privateKey;
    private boolean isCurrentUser;
    private String password;
    
    public User() {}

    public User(String fullName, String username, String email, String password, boolean admin) {
        this.fullName = fullName;
        this.username = username;
        this.email = email;
        this.admin = admin;
        this.password = password;
    }
    
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

    public PublicKey getPublicKey() {
        return new PublicKey(this.id, this.publicKey);
    }

    public void setPublicKey(byte[] publicKey) {
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

    public void setIsCurrentUser(boolean isCurrentUser) {
        this.isCurrentUser = isCurrentUser;
    }

    public boolean isIsCurrentUser() {
        return isCurrentUser;
    }
    
    public static User getCurrentFromServer() throws LockerRuntimeException {
        Locker locker = Locker.getInstance();
        try {           
            String response = locker.makeGetRequest("users/self").asJson().getBody().getObject().getJSONObject("user").toString();
            
            User user = locker.getObjectMapper().readValue(response, User.class);
            user.setIsCurrentUser(true);
            
            // Decrypts the private key
            user.setPrivateKey(Crypto.aesDecrypt(locker.getPassword(), user.getPbkdf2Salt(), user.getAesIv(), user.getEncryptedPrivateKey()));
                    
            return user;
        } catch (Exception e) {
            throw new LockerRuntimeException("Request Error:\n\n" + e.getMessage());
        }
    }
    
    public static User[] getAllFromServer() throws LockerRuntimeException {
        Locker locker = Locker.getInstance();
        try {
            JSONObject response = locker.makeGetRequest("users").asJson().getBody().getObject();
            
            if (!response.isNull("error")) {
                throw new LockerRuntimeException(response.getString("message"));
            }
                        
            User[] users = locker.getObjectMapper().readValue(response.getJSONArray("users").toString(), User[].class);
            
            return users;
        } catch (LockerRuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new LockerRuntimeException("Request Error:\n\n" + e.getMessage());
        }
    }
    
    public void changePasswordOnServer(String newPassword) throws LockerRuntimeException {
        if (newPassword.isEmpty()) {
            throw new LockerRuntimeException("You must enter a password!");
        }
        
        if (!this.isCurrentUser) {
            throw new LockerRuntimeException("You can only change the password for your own user!");
        }
        
        Locker locker = Locker.getInstance();

        try {
            Crypto.EncryptedPrivateKey encryptedPrivateKey = Crypto.encryptPrivateKey(newPassword, this.privateKey);
            
            String newAuthKey = Crypto.generateAuthKey(newPassword, this.username);

            Base64.Encoder encoder = Base64.getEncoder();
            
            String payload = (new JSONObject()
                    .put("encrypted_private_key", new String(encoder.encode(encryptedPrivateKey.getKey())))
                    .put("aes_iv", new String(encoder.encode(encryptedPrivateKey.getIv())))
                    .put("pbkdf2_salt", new String(encoder.encode(encryptedPrivateKey.getSalt())))
                    .put("auth_key", newAuthKey)).toString();
            
            JSONObject response = locker.makePostRequest("users/self/update_password").header("accept", "application/json").header("content-type", "application/json").body(
                            payload).asJson().getBody().getObject();
            
            if (!response.isNull("error")) {
                throw new LockerRuntimeException(response.getString("message"));
            }
        } catch (LockerRuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new LockerRuntimeException("Request Error:\n\n" + e.getMessage());
        }
    }
    
    // TODO: Add support for setting folder permissions when adding user - Admins should be granted access to all folders
    public User addToServer() throws LockerRuntimeException {
        Validation.ensureNonEmpty(this.username, "Username");
        Validation.ensureNonEmpty(this.password, "Password");
        Validation.ensureNonEmpty(this.fullName, "Full Name");
        Validation.ensureNonEmpty(this.email, "Email");
        
        Locker locker = Locker.getInstance();
        
        try {
            KeyPair keypair = Crypto.generateRsaKeyPair();
            this.privateKey = keypair.getPrivate().getEncoded();
            this.publicKey = keypair.getPublic().getEncoded();
            Crypto.EncryptedPrivateKey epk = Crypto.encryptPrivateKey(this.password, privateKey);
            this.encryptedPrivateKey = epk.getKey();
            this.aesIv = epk.getIv();
            this.pbkdf2Salt = epk.getSalt();
            String authKey = Crypto.generateAuthKey(this.password, this.username);
            
            Base64.Encoder encoder = Base64.getEncoder();
            
            String payload = (new JSONObject()
                    .put("encrypted_private_key", new String(encoder.encode(this.encryptedPrivateKey)))
                    .put("aes_iv", new String(encoder.encode(this.getAesIv())))
                    .put("pbkdf2_salt", new String(encoder.encode(this.getPbkdf2Salt())))
                    .put("public_key", new String(encoder.encode(this.publicKey)))
                    .put("auth_key", authKey)
                    .put("username", this.username)
                    .put("full_name", this.fullName)
                    .put("email", this.email)
                    .put("admin", this.admin)).toString();
            
            JSONObject response = locker.makePutRequest("users")
                    .header("accept", "application/json")
                    .header("content-type", "application/json")
                    .body(payload).asJson().getBody().getObject();
            
            if (!response.isNull("error")) {
                throw new LockerRuntimeException(response.getString("message"));
            }
            
            this.id = response.getInt("user_id");
            
            if (this.admin) {
                Folder[] folders = Folder.getAllFromServer();
                for (Folder folder : folders) {
                    folder.encryptForUser(this);
                }
            }
            
            return this;
        } catch (Exception e) {
            throw new LockerRuntimeException("Request Error:\n\n" + e.getMessage());
        }
    }
}
