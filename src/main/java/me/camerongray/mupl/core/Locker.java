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

/**
 * Created by camerong on 09/07/16.
 */
public class Locker {
    private String server;
    private int port;
    private String username;
    private String password;
    private String auth_key;

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
    }
    
    private String get_url(String path) {
        URIBuilder u = new URIBuilder();
        u.setScheme("http");
        u.setHost(this.server);
        u.setPort(this.port);
        u.setPath("/"+path+"/");
        return u.toString();
    }

    public boolean check_auth() throws LockerRuntimeException {
        try {
            JSONObject response = Unirest.get(this.get_url("check_auth")).basicAuth(this.username,
                    this.auth_key).asJson().getBody().getObject();
            return response.isNull("error");
        } catch (Exception e) {
            throw new LockerRuntimeException("Could not connect to server:\n\n" +
                    e.getMessage());
        }
    }
}