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

/**
 *
 * @author camerong
 */
public class Crypto {
    public static byte[] rsaDecryptWithPrivateKey(byte[] privateKey, byte[] encryptedData) throws CryptoException {
        try {
            Security.addProvider(new BouncyCastleProvider());
            Cipher rsaCipher = Cipher.getInstance("RSA/NONE/OAEPWithSHA1AndMGF1Padding", "BC");
            PKCS8EncodedKeySpec privateKeySpec = new PKCS8EncodedKeySpec(privateKey);
            KeyFactory kf = KeyFactory.getInstance("RSA");
            Key rsaKey = kf.generatePrivate(privateKeySpec);
            rsaCipher.init(Cipher.DECRYPT_MODE, rsaKey);
            
            return rsaCipher.doFinal(encryptedData);
        } catch (Exception e) {
            throw new CryptoException(e);
        }
    }
    
    public static byte[] rsaDecryptWithPrivateKey(String privateKey, byte[] encryptedData) throws CryptoException {
        return Crypto.rsaDecryptWithPrivateKey(Base64.getDecoder().decode(privateKey), encryptedData);
    }
    
    public static byte[] aesDecrypt(SecretKey key, byte[] iv, byte[] encryptedData) throws CryptoException {
        try {
            Cipher dcipher = Cipher.getInstance("AES/CFB8/NoPadding"); // CFB8 makes this byte oriented
            dcipher.init(Cipher.DECRYPT_MODE, key, new IvParameterSpec(iv));
            return dcipher.doFinal(encryptedData);
        } catch (Exception e) {
            throw new CryptoException(e);
        }
    }
    
    public static byte[] aesDecrypt(String key, String iv, byte[] encryptedData) throws CryptoException {
        return Crypto.aesDecrypt(Base64.getDecoder().decode(key), Base64.getDecoder().decode(iv), encryptedData);
    }
    
    public static byte[] aesDecrypt(byte[] key, byte[] iv, byte[] encryptedData) throws CryptoException {
        SecretKey aesKey = new SecretKeySpec(key, "AES");
        return Crypto.aesDecrypt(aesKey, iv, encryptedData);
    }
    
    public static byte[] aesDecrypt(String password, byte[] salt, byte[] iv, byte[] encryptedData) throws CryptoException {
        SecretKey key;
        try{
            final int ITERATION_COUNT = 1000;
            final int KEY_LENGTH = 256;
            SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
            KeySpec spec = new PBEKeySpec(password.toCharArray(), salt, ITERATION_COUNT, KEY_LENGTH);
            key = new SecretKeySpec(skf.generateSecret(spec).getEncoded(), "AES");
        } catch (Exception e) {
            throw new CryptoException(e);
        }
        return Crypto.aesDecrypt(key, iv, encryptedData);
    }
}
