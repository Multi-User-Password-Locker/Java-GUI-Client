/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package me.camerongray.teamlocker.core;

import java.io.IOException;
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
    
    public Folder(int id) {
        this.id = id;
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
    
    public static Folder[] getAllFromServer() throws LockerCommunicationException, IOException, LockerSimpleException {
        Locker locker = Locker.getInstance();
        String response = locker.makeGetRequest("folders").getJSONArray("folders").toString();

        Folder[] folders = locker.getObjectMapper().readValue(response, Folder[].class);
        return folders;
    }
    
    public void addToServer() throws LockerSimpleException, LockerCommunicationException {
        Locker locker = Locker.getInstance();
                
        String payload = new JSONObject().put("name", this.name).toString();
                
        JSONObject response = locker.makePutRequest("folders", payload);
        if (!response.isNull("error")) {
            throw new LockerSimpleException(response.getString("message"));
        }
                
        this.id = response.getInt("folder_id");
    }
    
    public FolderPermission[] getPermissionsFromServer() throws LockerSimpleException, LockerCommunicationException, IOException {
        Locker locker = Locker.getInstance();
        JSONObject response = locker.makeGetRequest("folders/"+this.id+"/permissions");


        if (!response.isNull("error")) {
            throw new LockerSimpleException(response.getString("message"));
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
    }
    
    public void deleteFromServer() throws LockerSimpleException, LockerCommunicationException {
        Locker locker = Locker.getInstance();
        JSONObject response = locker.makeDeleteRequest("folders/"+this.id);

        if (!response.isNull("error")) {
            throw new LockerSimpleException(response.getString("message"));
        }
    }
    
    public void updatePermissionsOnServer(FolderPermission permission) throws LockerSimpleException, LockerCommunicationException, CryptoException, LockerRuntimeException {
        FolderPermission[] permissions = new FolderPermission[]{permission};
        ArrayList<Integer> newReadUsers = new ArrayList<Integer>();
        if (permission.isRead()) {
            newReadUsers.add(permission.getUser().getId());
        }
        this.updatePermissionsOnServer(permissions, newReadUsers);
    }
    
    // TODO - Move into FolderPermission.java?
    public void updatePermissionsOnServer(FolderPermission[] permissions, ArrayList<Integer> newReadUsers) throws LockerSimpleException, LockerCommunicationException, CryptoException, LockerRuntimeException {
        Locker locker = Locker.getInstance();
        locker.startTransaction();
        
        try {
            JSONArray json_permissions = new JSONArray();
            for (FolderPermission p : permissions) {
                JSONObject permission = new JSONObject();
                permission.put("user_id", p.getUser().getId());
                permission.put("read", p.isRead());
                permission.put("write", p.isWrite());
                json_permissions.put(permission);
            }

            JSONObject payload = new JSONObject();
            payload.put("permissions", json_permissions);
            JSONObject response = locker.makePostRequest("folders/" + this.id + "/permissions", payload.toString());

            if (!response.isNull("error")) {
                throw new LockerSimpleException(response.getString("message"));
            }

            // This must happen AFTER the folder permissions have been updated!
            PublicKey[] publicKeys = this.getPublicKeysFromServer();

            this.encryptForUsers(newReadUsers, publicKeys);
            locker.commitTransaction();
        } catch (Exception ex) {
            locker.rollbackTransaction();
            throw ex;
        }
    }
    
    public void encryptForUser(User user) throws LockerSimpleException, CryptoException, LockerCommunicationException, LockerRuntimeException {
        PublicKey[] publicKeys = new PublicKey[]{user.getPublicKey()};
        ArrayList<Integer> newReadUsers = new ArrayList<>();
        newReadUsers.add(user.getId());
        
        this.encryptForUsers(newReadUsers, publicKeys);
    }
    
    public void encryptForUsers(ArrayList<Integer> newReadUsers, PublicKey[] publicKeys) throws LockerSimpleException, CryptoException, LockerCommunicationException, LockerRuntimeException {
        User currentUser = CurrentUser.getInstance();
        Locker locker = Locker.getInstance();
        if (newReadUsers != null && newReadUsers.size() > 0) {
            JSONArray encryptedAccounts = new JSONArray();
            Account[] accounts = this.getAccountsFromServer(currentUser.getPrivateKey());
            for (Account a : accounts) {
                a.setPassword(a.getPasswordFromServer(currentUser.getPrivateKey()));
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
            JSONObject payload = new JSONObject().put("accounts", encryptedAccounts);
            JSONObject response = locker.makePostRequest("accounts", payload.toString());

            if (!response.isNull("error")) {
                throw new LockerSimpleException(response.getString("message"));
            }
        }
    }
    
    public PublicKey[] getPublicKeysFromServer() throws LockerSimpleException, LockerCommunicationException {
        Locker locker = Locker.getInstance();
        ArrayList<PublicKey> publicKeys = new ArrayList<>();
        JSONObject response = locker.makeGetRequest("folders/"+this.id+"/public_keys");            

        if (!response.isNull("error")) {
            throw new LockerSimpleException(response.getString("message"));
        }

        // TODO - Rename accountArray!
        JSONArray accountArray = response.getJSONArray("public_keys");
        for (int i = 0; i < accountArray.length(); i++) {
            JSONObject row = accountArray.getJSONObject(i);
            publicKeys.add(new PublicKey(row.getInt("user_id"),
                    Base64.getDecoder().decode(row.getString("public_key"))));
        }
        return publicKeys.toArray(new PublicKey[publicKeys.size()]);
    }
    
    public Account[] getAccountsFromServer(byte[] privateKey) throws LockerSimpleException, LockerCommunicationException, CryptoException {
        Locker locker = Locker.getInstance();
        ArrayList<Account> accounts = new ArrayList<>();
        
        JSONObject response = locker.makeGetRequest("folders/"+this.id+"/accounts");            

        if (!response.isNull("error")) {
            throw new LockerSimpleException(response.getString("message"));
        }
        
        JSONArray accountArray = response.getJSONArray("accounts");
        for (int i = 0; i < accountArray.length(); i++) {
            JSONObject accountObject = accountArray.getJSONObject(i);
            byte[] encryptedMetadata = Base64.getDecoder().decode(accountObject.getString("account_metadata"));
            byte[] encryptedAesKey = Base64.getDecoder().decode(accountObject.getString("encrypted_aes_key"));
            accounts.add(new Account(accountObject.getInt("id"), encryptedMetadata, encryptedAesKey, privateKey));
        }

        return accounts.toArray(new Account[accounts.size()]);
    }
    
    public void updateOnServer() throws LockerSimpleException, LockerCommunicationException {
        JSONObject payload = new JSONObject();
        payload.put("name", this.name);
        Locker locker = Locker.getInstance();
        JSONObject response = locker.makePostRequest("folders/" + this.id, payload.toString());

        if (!response.isNull("error")) {
            throw new LockerSimpleException(response.getString("message"));
        }
    }
}
