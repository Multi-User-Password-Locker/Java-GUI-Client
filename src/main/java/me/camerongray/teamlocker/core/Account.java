/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package me.camerongray.teamlocker.core;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import java.util.ArrayList;
import java.util.Base64;
import org.json.JSONArray;
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

    public Account(String name, String username, String notes, String password) {
        this.name = name;
        this.username = username;
        this.notes = notes;
        this.password = password;
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
    
    public void deleteFromServer() throws LockerRuntimeException, UnirestException {
        Locker locker = Locker.getInstance();
        JSONObject response = locker.makeDeleteRequest("accounts/"+this.id).asJson().getBody().getObject();

        if (response.isNull("success")) {
            throw new LockerRuntimeException(response.getString("message"));
        }
    }
    
    public String getPasswordFromServer(byte[] privateKey) throws LockerRuntimeException, CryptoException, UnirestException {
        Locker locker = Locker.getInstance();
        JSONObject response = locker.makeGetRequest("accounts/"+this.id+"/password").asJson().getBody().getObject();            

        if (!response.isNull("error")) {
            throw new LockerRuntimeException(response.getString("message"));
        }

        JSONObject accountObject = response.getJSONObject("password");
        byte[] encryptedPassword = Base64.getDecoder().decode(accountObject.getString("encrypted_password"));
        byte[] encryptedAesKey = Base64.getDecoder().decode(accountObject.getString("encrypted_aes_key"));

        JSONObject aesKeyParts = new JSONObject(new String(Crypto.rsaDecryptWithPrivateKey(privateKey, encryptedAesKey)));

        String password = new String(Crypto.aesDecrypt(aesKeyParts.getString("key"), aesKeyParts.getString("iv"), encryptedPassword));
        return password;
    }
    
    public int addToServer(Folder folder) throws LockerRuntimeException, UnirestException, CryptoException {    
        Locker locker = Locker.getInstance();
        Base64.Encoder encoder = Base64.getEncoder();
        JSONArray accounts = new JSONArray();
        EncryptedAccount[] encryptedAccounts = this.encrypt(folder);
        for (EncryptedAccount account : encryptedAccounts) {

            accounts.put(new JSONObject()
                    .put("user_id", account.getUserId())
                    .put("password", new String(encoder.encode(account.getEncryptedPassword())))
                    .put("account_metadata", new String(encoder.encode(account.getEncryptedMetadata())))
                    .put("encrypted_aes_key", new String(encoder.encode(account.getEncryptedAesKey()))));
        }
        String payload = new JSONObject().put("folder_id", folder.getId())
                .put("encrypted_account_data", accounts).toString();

        JSONObject response = locker.makePutRequest("accounts/add").header("accept", "application/json").header("content-type", "application/json").body(
                        payload).asJson().getBody().getObject();

        if (!response.isNull("error")) {
            throw new LockerRuntimeException(response.getString("message"));
        }

        int accountId = response.getInt("account_id");
        return accountId;
    }
    
    public EncryptedAccount[] encrypt(Folder folder) throws LockerRuntimeException, CryptoException {
        return this.encrypt(folder, null);
    }

    public EncryptedAccount[] encrypt(Folder folder, ArrayList<Integer> userIds) throws LockerRuntimeException, CryptoException {
        PublicKey[] publicKeys = folder.getPublicKeysFromServer();
        return this.encrypt(userIds, publicKeys);
    }
    
    public EncryptedAccount[] encrypt(ArrayList<Integer> userIds, PublicKey[] publicKeys) throws LockerRuntimeException, CryptoException {
        byte[] metadataBytes = (new JSONObject()
                .put("name", name)
                .put("notes", notes)
                .put("username", username)).toString().getBytes();


        ArrayList<EncryptedAccount> encryptedAccounts = new ArrayList<>();
        for (PublicKey key : publicKeys) {
            if (userIds == null || userIds.contains(key.getUserId())) {
                Crypto.AesKey aesKey = Crypto.generateAesKey();
                byte[] encryptedMetadata = Crypto.aesEncrypt(aesKey, metadataBytes);
                byte[] encryptedPassword = Crypto.aesEncrypt(aesKey, password.getBytes());
                byte[] encryptedAesKey = Crypto.rsaEncryptAesKey(key.getKey(), aesKey);

                encryptedAccounts.add(new EncryptedAccount(key.getUserId(), encryptedMetadata, encryptedPassword, encryptedAesKey));
            }
        }

        return encryptedAccounts.toArray(new EncryptedAccount[encryptedAccounts.size()]);
    }
    
    public EncryptedAccount[] encrypt(PublicKey[] publicKeys) throws LockerRuntimeException, CryptoException {
        return this.encrypt(null, publicKeys);
    }
    
    public void updateOnServer(Folder folder) throws LockerRuntimeException, CryptoException, UnirestException {      
        Base64.Encoder encoder = Base64.getEncoder();
        JSONArray accounts = new JSONArray();        
        Locker locker = Locker.getInstance();
        EncryptedAccount[] encryptedAccounts = this.encrypt(folder);
        for (EncryptedAccount account : encryptedAccounts) {

            accounts.put(new JSONObject()
                    .put("user_id", account.getUserId())
                    .put("password", new String(encoder.encode(account.getEncryptedPassword())))
                    .put("account_metadata", new String(encoder.encode(account.getEncryptedMetadata())))
                    .put("encrypted_aes_key", new String(encoder.encode(account.getEncryptedAesKey()))));
        }
        String payload = new JSONObject().put("folder_id", folder.getId())
                .put("encrypted_account_data", accounts).toString();

        JSONObject response = locker.makePostRequest("accounts/"+this.id)
                .header("accept", "application/json")
                .header("content-type", "application/json")
                .body(payload).asJson().getBody().getObject();

        if (!response.isNull("error")) {
            throw new LockerRuntimeException(response.getString("message"));
        }
    }
    
    public static Account getFromServer(int accountId, byte[] privateKey) throws LockerRuntimeException, CryptoException, UnirestException {
        Locker locker = Locker.getInstance();
        JSONObject response = locker.makeGetRequest("accounts/"+accountId).asJson().getBody().getObject();            

        if (!response.isNull("error")) {
            throw new LockerRuntimeException(response.getString("message"));
        }

        JSONObject accountObject = response.getJSONObject("account");
        byte[] encryptedMetadata = Base64.getDecoder().decode(accountObject.getString("account_metadata"));
        byte[] encryptedAesKey = Base64.getDecoder().decode(accountObject.getString("encrypted_aes_key"));

        Account account = new Account(accountObject.getInt("id"), encryptedMetadata, encryptedAesKey, privateKey);
        return account;
    }
}
