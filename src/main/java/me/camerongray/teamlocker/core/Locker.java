package me.camerongray.teamlocker.core;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.Base64;
import com.mashape.unirest.http.Unirest;
import org.json.JSONObject;
import org.apache.http.client.utils.URIBuilder;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.mashape.unirest.http.exceptions.UnirestException;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * Created by camerong on 09/07/16.
 */
public class Locker {
    private static Locker instance = null;
    private String server;
    private int port;
    private String username;
    private String password;
    private String auth_key;
    private ObjectMapper objectMapper;
    private Transaction transaction = null;
    
    protected Locker() {
        // Prevent instantiation
    }
    
    public static Locker getInstance() {
        if (instance == null) {
            instance = new Locker();
        }
        return instance;
    }

    public void init(String server, int port, String username, String password) throws LockerRuntimeException {

        try {
            KeySpec spec = new PBEKeySpec(password.toCharArray(), username.getBytes(), 100000, 512);
            SecretKeyFactory f = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA512");
            this.auth_key = Base64.getEncoder().encodeToString(f.generateSecret(spec).getEncoded());
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new LockerRuntimeException(e);
        }
        this.server = server;
        this.port = port;
        this.username = username;
        this.password = password;
        
        this.objectMapper = new ObjectMapper();
        this.objectMapper.setPropertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE);
    }
    
    public String getUrl(String path) {
        URIBuilder u = new URIBuilder();
        u.setScheme("http");
        u.setHost(this.server);
        u.setPort(this.port);
        u.setPath("/"+path+"/");
        if (this.transaction != null) {
            u.addParameter("transaction_id", this.transaction.getId());
        }
        System.out.println(u.toString());
        return u.toString();
    }
    
    public JSONObject makeGetRequest(String url) throws LockerCommunicationException, LockerSimpleException {
        try {
            JSONObject response = Unirest.get(this.getUrl(url)).basicAuth(this.username,
                    this.auth_key).asJson().getBody().getObject();
            this.handleMissingTransaction(response);
            return response;
        } catch (UnirestException e) {
            throw new LockerCommunicationException(e);
        }
    }

    public JSONObject makePostRequest(String url, String payload) throws LockerCommunicationException, LockerSimpleException {
        try {
            JSONObject response = Unirest.post(this.getUrl(url))
                    .basicAuth(this.username, this.auth_key)
                    .header("accept", "application/json")
                    .header("content-type", "application/json")
                    .body(payload).asJson().getBody().getObject();
            this.handleMissingTransaction(response);
            return response;
        } catch (UnirestException e) {
            throw new LockerCommunicationException(e);
        }
    }
    
    public JSONObject makePutRequest(String url, String payload) throws LockerCommunicationException, LockerSimpleException {
        try {
            JSONObject response = Unirest.put(this.getUrl(url))
                    .basicAuth(this.username, this.auth_key)
                    .header("accept", "application/json")
                    .header("content-type", "application/json")
                    .body(payload).asJson().getBody().getObject();
            this.handleMissingTransaction(response);
            return response;
        } catch (UnirestException e) {
            throw new LockerCommunicationException(e);
        }
    }

    public JSONObject makeDeleteRequest(String url) throws LockerCommunicationException, LockerSimpleException {
        try {
            JSONObject response = Unirest.delete(this.getUrl(url))
                    .basicAuth(this.username, this.auth_key)
                    .asJson().getBody().getObject();
            this.handleMissingTransaction(response);
            return response;
        } catch (UnirestException e) {
            throw new LockerCommunicationException(e);
        }
    }
    
    private void handleMissingTransaction(JSONObject response) throws LockerSimpleException {
        if (!response.isNull("error") && !response.isNull("type")) {
            if (response.getString("type").equals("transaction_not_found")) {
                this.transaction = null;
                throw new LockerSimpleException("The current transaction could not be found on the "
                        + "server.\n\nPlease try your last operation again.");
            }
        }
    }

    public boolean checkAuth() throws LockerCommunicationException, LockerSimpleException {
        JSONObject response;
        try {
            response = Unirest.get(this.getUrl("check_auth")).basicAuth(this.username,
                    this.auth_key).asJson().getBody().getObject();
        } catch (Exception e) {
            throw new LockerCommunicationException("Could not connect to server, check your network connection", e);
        }
        if (!response.isNull("error")) {
            throw new LockerSimpleException(response.getString("message"));
        }
        return true;
    }
    
    public void startTransaction() throws LockerSimpleException, LockerCommunicationException {
        // TODO - What do we want to do if a transaction is always in progress
        if (this.transaction == null ) {
            this.transaction = new Transaction();
        }
    }
    
    public void commitTransaction() throws LockerCommunicationException, LockerSimpleException {
        this.transaction.commit();
        this.transaction = null;
    }
    
    public void rollbackTransaction() throws LockerCommunicationException, LockerSimpleException {
        this.transaction.rollback();
        this.transaction = null;
    }
    
    public ObjectMapper getObjectMapper() {
        return this.objectMapper;
    }
    
    public String getPassword() {
        return this.password;
    }
}

class EncryptedAccount {
    int userId;
    byte[] encryptedMetadata;
    byte[] encryptedPassword;
    byte[] encryptedAesKey;

    public EncryptedAccount(int user_id, byte[] encryptedMetadata, byte[] encryptedPassword, byte[] encryptedAesKey) {
        this.userId = user_id;
        this.encryptedMetadata = encryptedMetadata;
        this.encryptedPassword = encryptedPassword;
        this.encryptedAesKey = encryptedAesKey;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public byte[] getEncryptedMetadata() {
        return encryptedMetadata;
    }

    public void setEncryptedMetadata(byte[] encryptedMetadata) {
        this.encryptedMetadata = encryptedMetadata;
    }

    public byte[] getEncryptedPassword() {
        return encryptedPassword;
    }

    public void setEncryptedPassword(byte[] encryptedPassword) {
        this.encryptedPassword = encryptedPassword;
    }

    public byte[] getEncryptedAesKey() {
        return encryptedAesKey;
    }

    public void setEncryptedAesKey(byte[] encryptedAesKey) {
        this.encryptedAesKey = encryptedAesKey;
    }
}

class EncryptedAesKey {
    private int accountId;
    private byte[] encryptedAesKey;

    public EncryptedAesKey(int accountId, byte[] encryptedAesKey) {
        this.accountId = accountId;
        this.encryptedAesKey = encryptedAesKey;
    }

    public int getAccountId() {
        return accountId;
    }

    public void setAccountId(int accountId) {
        this.accountId = accountId;
    }

    public byte[] getEncryptedAesKey() {
        return encryptedAesKey;
    }

    public void setEncryptedAesKey(byte[] encryptedAesKey) {
        this.encryptedAesKey = encryptedAesKey;
    }
}