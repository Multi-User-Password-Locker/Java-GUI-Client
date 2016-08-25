/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package me.camerongray.teamlocker.core;

import com.mashape.unirest.http.Unirest;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import org.json.JSONArray;
import org.json.JSONObject;

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
    
    public Folder(String name) {
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
    
    public static Folder[] getAllFromServer() throws LockerRuntimeException {
        Locker locker = Locker.getInstance();
        try {
            String response = locker.makeGetRequest("folders").asJson().getBody()
                    .getObject().getJSONArray("folders").toString();
            
            Folder[] folders = locker.getObjectMapper().readValue(response, Folder[].class);
            return folders;
        } catch (Exception e) {
            throw new LockerRuntimeException("Request Error:\n\n" + e.getMessage());
        }
    }
    
    public void addToServer() throws LockerRuntimeException {
        Locker locker = Locker.getInstance();
        
        try {
            String payload = new JSONObject().put("name", this.name).toString();
            
            JSONObject response = locker.makePutRequest("folders")
                    .header("accept", "application/json")
                    .header("content-type", "application/json").body(
                            payload).asJson().getBody().getObject();
            if (response.isNull("success")) {
                throw new LockerRuntimeException(response.getString("message"));
            }
            
            this.id = response.getInt("folder_id");
        } catch (LockerRuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new LockerRuntimeException("Request Error:\n\n" + e.getMessage());
        }
    }
    
    public FolderPermission[] getPermissionsFromServer() throws LockerRuntimeException {
        try {
            Locker locker = Locker.getInstance();
            JSONObject response = locker.makeGetRequest("folders/"+this.id+"/permissions")
                    .asJson().getBody().getObject();
            
            
            if (!response.isNull("error")) {
                throw new LockerRuntimeException(response.getString("message"));
            }

            JSONArray permissions = response.getJSONArray("permissions");
            HashMap<Integer, JSONObject> permissionMap = new HashMap<>();
            for (int i = 0; i < permissions.length(); i++) {
                JSONObject permission = permissions.getJSONObject(i);
                permissionMap.put(permission.getInt("user_id"), permission);
            }
            
            User[] users = User.getAllFromServer();
            ArrayList<FolderPermission> folderPermissions = new ArrayList<>();
            for (User user : users) {
                boolean read = false;
                boolean write = false;
                if (permissionMap.containsKey(user.getId())) {
                    JSONObject permission = permissionMap.get(user.getId());
                    read = permission.getBoolean("read");
                    write = permission.getBoolean("write");
                }
                folderPermissions.add(new FolderPermission(user, this, read, write));
            }
            
            return folderPermissions.toArray(new FolderPermission[folderPermissions.size()]);
        } catch (LockerRuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new LockerRuntimeException("Request Error:\n\n" + e.getMessage());
        }
    }
    
    public void deleteFromServer() throws LockerRuntimeException {
        Locker locker = Locker.getInstance();
        try {
            JSONObject response = locker.makeDeleteRequest("folders/delete/"+this.id)
                    .asJson().getBody().getObject();
            
            if (response.isNull("success")) {
                throw new LockerRuntimeException(response.getString("message"));
            }
         
        } catch (LockerRuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new LockerRuntimeException("Request Error:\n\n" + e.getMessage());
        }
    }
    
    public void updatePermissionsOnServer(byte[] privateKey, FolderPermission[] permissions, ArrayList<Integer> newReadUsers) throws LockerRuntimeException {
        Locker locker = Locker.getInstance();
        
        JSONArray json_permissions = new JSONArray();
        for (FolderPermission p : permissions) {
            JSONObject permission = new JSONObject();
            permission.put("user_id", p.getUser().getId());
            permission.put("read", p.isRead());
            permission.put("write", p.isWrite());
            json_permissions.put(permission);
        }

        try {
            JSONObject payload = new JSONObject();
            payload.put("permissions", json_permissions);
            JSONObject response = locker.makePostRequest("folders/" + this.id + "/permissions")
                    .header("accept", "application/json")
                    .header("content-type", "application/json")
                    .body(payload.toString()).asJson().getBody().getObject();
            
            if (response.isNull("success")) {
                throw new LockerRuntimeException(response.getString("message"));
            }
            
            // This must happen AFTER the folder permissions have been updated!
            PublicKey[] publicKeys = this.getPublicKeysFromServer();
            
            if (newReadUsers != null && newReadUsers.size() > 0) {
                JSONArray encryptedAccounts = new JSONArray();
                Account[] accounts = this.getAccountsFromServer(privateKey);
                for (Account a : accounts) {
                    a.setPassword(a.getPasswordFromServer(privateKey));
                    // TODO - encryptAccount requests all public keys for a folder every call, cache them somehow as always requesting same?
                    EncryptedAccount[] eas = a.encrypt(newReadUsers, publicKeys);
                    JSONArray ead = new JSONArray();
                    for (EncryptedAccount ea : eas) {
                        ead.put(new JSONObject()
                                .put("user_id", ea.getUserId())
                                .put("password", new String(Base64.getEncoder().encode(ea.getEncryptedPassword())))
                                .put("account_metadata", new String(Base64.getEncoder().encode(ea.getEncryptedMetadata())))
                                .put("encrypted_aes_key", new String(Base64.getEncoder().encode(ea.getEncryptedAesKey()))));
                    }
                    encryptedAccounts.put(new JSONObject()
                            .put("account_id", a.getId())
                            .put("encrypted_account_data", ead));
                }
                
                response = locker.makePostRequest("accounts")
                        .header("accept", "application/json")
                        .header("content-type", "application/json")
                        .body(encryptedAccounts.toString()).asJson().getBody().getObject();
            
                if (response.isNull("success")) {
                    throw new LockerRuntimeException(response.getString("message"));
                }
            }
            
        } catch (LockerRuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new LockerRuntimeException("Request Error:\n\n" + e.getMessage());
        }
    }
    
    public PublicKey[] getPublicKeysFromServer() throws LockerRuntimeException {
        Locker locker = Locker.getInstance();
        ArrayList<PublicKey> publicKeys = new ArrayList<>();
        try {
            JSONObject response = locker.makeGetRequest("folders/"+this.id+"/public_keys")
                    .asJson().getBody().getObject();            
            
            if (!response.isNull("error")) {
                throw new LockerRuntimeException(response.getString("message"));
            }
            
            // TODO - Rename accountArray!
            JSONArray accountArray = response.getJSONArray("public_keys");
            for (int i = 0; i < accountArray.length(); i++) {
                JSONObject row = accountArray.getJSONObject(i);
                publicKeys.add(new PublicKey(row.getInt("user_id"),
                        Base64.getDecoder().decode(row.getString("public_key"))));
            }
            
        } catch (LockerRuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new LockerRuntimeException("Request Error:\n\n" + e.getMessage());
        }
        return publicKeys.toArray(new PublicKey[publicKeys.size()]);
    }
    
    public Account[] getAccountsFromServer(byte[] privateKey) throws LockerRuntimeException {
        Locker locker = Locker.getInstance();
        ArrayList<Account> accounts = new ArrayList<>();
        try {
            JSONObject response = locker.makeGetRequest("folders/"+this.id+"/accounts")
                    .asJson().getBody().getObject();            
            
            if (!response.isNull("error")) {
                throw new LockerRuntimeException(response.getString("message"));
            }
            
            JSONArray accountArray = response.getJSONArray("accounts");
            for (int i = 0; i < accountArray.length(); i++) {
                JSONObject accountObject = accountArray.getJSONObject(i);
                byte[] encryptedMetadata = Base64.getDecoder().decode(accountObject.getString("account_metadata"));
                byte[] encryptedAesKey = Base64.getDecoder().decode(accountObject.getString("encrypted_aes_key"));
                accounts.add(new Account(accountObject.getInt("id"), encryptedMetadata, encryptedAesKey, privateKey));
            }
            
            return accounts.toArray(new Account[accounts.size()]);
        } catch (LockerRuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new LockerRuntimeException("Request Error:\n\n" + e.getMessage());
        }
    }
    
    public void updateOnServer() throws LockerRuntimeException {
        JSONObject payload = new JSONObject();
        payload.put("name", this.name);
        Locker locker = Locker.getInstance();
        try {
            JSONObject response = locker.makePostRequest("folders/" + this.id + "/save")
                    .header("accept", "application/json")
                    .header("content-type", "application/json")
                    .body(payload.toString()).asJson().getBody().getObject();
            
            if (response.isNull("success")) {
                throw new LockerRuntimeException(response.getString("message"));
            }
            
        } catch (LockerRuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new LockerRuntimeException("Request Error:\n\n" + e.getMessage());
        }
    }
}
