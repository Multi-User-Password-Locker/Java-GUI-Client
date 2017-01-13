/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package me.camerongray.teamlocker.core;

import org.json.JSONObject;

/**
 *
 * @author camerong
 */
public class Transaction {
    String id;
    
    public Transaction() throws LockerSimpleException, LockerCommunicationException {
        Locker locker = Locker.getInstance();
        JSONObject response = locker.makePutRequest("transaction", "");
        
        if (!response.isNull("error")) {
            throw new LockerSimpleException(response.getString("message"));
        }
        
        this.id = response.getString("transaction_id");
    }
        
    public void commit() throws LockerCommunicationException, LockerSimpleException {
        Locker locker = Locker.getInstance();
        JSONObject response = locker.makePostRequest("transaction/"+this.id+"/commit", "");
        
        if (!response.isNull("error")) {
            throw new LockerSimpleException(response.getString("message"));
        }
    }
    
    public void rollback() throws LockerCommunicationException, LockerSimpleException {
        Locker locker = Locker.getInstance();
        JSONObject response = locker.makePostRequest("transaction/"+this.id+"/rollback", "");
        
        if (!response.isNull("error")) {
            throw new LockerSimpleException(response.getString("message"));
        }
    }

    public String getId() {
        return id;
    }
}
