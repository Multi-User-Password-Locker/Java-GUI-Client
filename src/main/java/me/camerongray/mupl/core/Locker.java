package me.camerongray.mupl.core;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.Base64;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.exceptions.UnirestException;
import org.json.JSONObject;
import org.json.JSONException;
import org.apache.http.client.utils.URIBuilder;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;


/**
 * Created by camerong on 09/07/16.
 */
public class Locker {
    private String server;
    private int port;
    private String username;
    private String password;
    private String auth_key;
    private ObjectMapper objectMapper;

    public Locker(String server, int port, String username, String password) throws LockerRuntimeException {

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
    
    private String getUrl(String path) {
        URIBuilder u = new URIBuilder();
        u.setScheme("http");
        u.setHost(this.server);
        u.setPort(this.port);
        u.setPath("/"+path+"/");
        return u.toString();
    }

    public boolean checkAuth() throws LockerRuntimeException {
        try {
            JSONObject response = Unirest.get(this.getUrl("check_auth")).basicAuth(this.username,
                    this.auth_key).asJson().getBody().getObject();
            return response.isNull("error");
        } catch (Exception e) {
            throw new LockerRuntimeException("Could not connect to server:\n\n" +
                    e.getMessage());
        }
    }
    
    public User getCurrentUser() throws LockerRuntimeException {
        try {
            String response = Unirest.get(this.getUrl("users/self")).basicAuth(this.username,
                    this.auth_key).asJson().getBody().getObject().getJSONObject("user").toString();
            
            User user = this.objectMapper.readValue(response, User.class);
            
            return user;
        } catch (Exception e) {
            throw new LockerRuntimeException("Request Error:\n\n" + e.getMessage());
        }
    }
    
    public User[] getAllUsers() throws LockerRuntimeException {
        try {
            JSONObject response = Unirest.get(this.getUrl("users")).basicAuth(this.username,
                    this.auth_key).asJson().getBody().getObject();
            
            if (!response.isNull("error")) {
                throw new LockerRuntimeException(response.getString("message"));
            }
            
            User[] users = this.objectMapper.readValue(response.getJSONArray("users").toString(), User[].class);
            
            return users;
        } catch (LockerRuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new LockerRuntimeException("Request Error:\n\n" + e.getMessage());
        }
    }
    
    public Folder[] getFolders() throws LockerRuntimeException {
        try {
            String response = Unirest.get(this.getUrl("folders")).basicAuth(this.username,
                    this.auth_key).asJson().getBody().getObject().getJSONArray("folders").toString();
            
            Folder[] folders = this.objectMapper.readValue(response, Folder[].class);
            return folders;
        } catch (Exception e) {
            throw new LockerRuntimeException("Request Error:\n\n" + e.getMessage());
        }
    }
    
    public void addFolder(String folderName) throws LockerRuntimeException {
        JSONObject obj = new JSONObject();
        obj.put("name", folderName);
        JSONObject response;
        try {
            response = Unirest.put(this.getUrl("folders/add")).basicAuth(this.username,
                    this.auth_key).header("accept", "application/json").header("content-type", "application/json").body(
                            obj.toString()).asJson().getBody().getObject();
        } catch (Exception e) {
            throw new LockerRuntimeException("Request Error:\n\n" + e.getMessage());
        }
            
        if (response.isNull("success")) {
            throw new LockerRuntimeException(response.getString("message"));
        }
    }
    
    public void deleteFolder(int folderId) throws LockerRuntimeException {
        JSONObject response;
        try {
            response = Unirest.delete(this.getUrl("folders/delete/"+folderId)).basicAuth(
                    this.username, this.auth_key).asJson().getBody().getObject();
        } catch (Exception e) {
            throw new LockerRuntimeException("Request Error:\n\n" + e.getMessage());
        }
        
        if (response.isNull("success")) {
            throw new LockerRuntimeException(response.getString("message"));
        }
    }
    
    public void getAccounts(int folderId) throws LockerRuntimeException {
        
    }
}