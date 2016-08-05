/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package me.camerongray.teamlocker.core;

import java.security.Key;
import java.security.KeyFactory;
import java.security.SecureRandom;
import java.security.Security;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.KeySpec;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.json.JSONObject;

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
        SecretKey key = Crypto.keyFromPassword(password, salt);
        return Crypto.aesDecrypt(key, iv, encryptedData);
    }
    
    public static byte[] aesEncrypt(byte[] key, byte[] iv, byte[] data) throws CryptoException {
        return aesEncrypt(new SecretKeySpec(key, "AES"), iv, data);
    }
    
    public static byte[] aesEncrypt(AesKey key, byte[] data) throws CryptoException {
        return aesEncrypt(new SecretKeySpec(key.getKey(), "AES"), key.getIv(), data);
    }    
    
    public static byte[] aesEncrypt(SecretKey key, byte[] iv, byte[] data) throws CryptoException {
        try {
            Cipher aesCipher = Cipher.getInstance("AES/CFB8/NoPadding");
            aesCipher.init(Cipher.ENCRYPT_MODE, key, new IvParameterSpec(iv));
            return aesCipher.doFinal(data);
        } catch (Exception e) {
            throw new CryptoException(e);
        }
    }
    
    public static SecretKey keyFromPassword(String password, byte[] salt) throws CryptoException {
        final int ITERATION_COUNT = 1000;
        final int KEY_LENGTH = 256;
       
        try{
            SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
            KeySpec spec = new PBEKeySpec(password.toCharArray(), salt, ITERATION_COUNT, KEY_LENGTH);
            SecretKey key = new SecretKeySpec(skf.generateSecret(spec).getEncoded(), "AES");
            return key;
        } catch (Exception e) {
            throw new CryptoException(e);
        }
    }
    
        
    public static EncryptedPrivateKey encryptPrivateKey(String password, byte[] privateKey) throws CryptoException {
        final int IV_BYTES = 16;
        final int SALT_BYTES = 8;
        
        SecureRandom random = new SecureRandom();
        byte[] iv = new byte[IV_BYTES];
        byte[] salt = new byte[SALT_BYTES];
        random.nextBytes(iv);
        random.nextBytes(salt);
        
        SecretKey key = keyFromPassword(password, salt);
        byte[] encryptedPrivateKey = aesEncrypt(key, iv, privateKey);
        
        return new EncryptedPrivateKey(encryptedPrivateKey, iv, salt);
    }
    
    public static PublicKeyEncrypted rsaEncryptWithPublicKey(byte[] publicKey, byte[] data) throws CryptoException {
        PublicKeyEncrypted pke = null;
        try {
            Crypto.AesKey aesKey = Crypto.generateAesKey();
            byte[] encryptedData = Crypto.aesEncrypt(aesKey, data);
            
            byte[] aesKeyUnencrypted = (new JSONObject()
                    .put("key", new String(Base64.getEncoder().encode(aesKey.getKey())))
                    .put("iv", new String(Base64.getEncoder().encode(aesKey.getIv())))).toString().getBytes();
            
            Security.addProvider(new BouncyCastleProvider());
            Cipher rsaCipher = Cipher.getInstance("RSA/NONE/OAEPWithSHA1AndMGF1Padding", "BC");
            X509EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(publicKey);
            KeyFactory kf = KeyFactory.getInstance("RSA");
            Key rsaKey = kf.generatePublic(publicKeySpec);
            rsaCipher.init(Cipher.ENCRYPT_MODE, rsaKey);
            
            byte[] encryptedAesKey = rsaCipher.doFinal(aesKeyUnencrypted);
            pke = new PublicKeyEncrypted(encryptedAesKey, encryptedData);
        } catch (Exception e) {
            throw new CryptoException(e);
        }
        return pke;
    }
    
    public static byte[] rsaEncryptAesKey(byte[] publicKey, Crypto.AesKey aesKey) throws CryptoException {
        try {           
            byte[] aesKeyUnencrypted = (new JSONObject()
                    .put("key", new String(Base64.getEncoder().encode(aesKey.getKey())))
                    .put("iv", new String(Base64.getEncoder().encode(aesKey.getIv())))).toString().getBytes();
            
            Security.addProvider(new BouncyCastleProvider());
            Cipher rsaCipher = Cipher.getInstance("RSA/NONE/OAEPWithSHA1AndMGF1Padding", "BC");
            X509EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(publicKey);
            KeyFactory kf = KeyFactory.getInstance("RSA");
            Key rsaKey = kf.generatePublic(publicKeySpec);
            rsaCipher.init(Cipher.ENCRYPT_MODE, rsaKey);
            
            byte[] encryptedAesKey = rsaCipher.doFinal(aesKeyUnencrypted);
            return encryptedAesKey;
        } catch (Exception e) {
            throw new CryptoException(e);
        }
    }
    
    public static Crypto.AesKey generateAesKey() {
        final int IV_BYTES = 16;
        final int KEY_BYTES = 32;
        SecureRandom random = new SecureRandom();
        byte[] iv = new byte[IV_BYTES];
        byte[] key = new byte[KEY_BYTES];
        random.nextBytes(iv);
        random.nextBytes(key);
        return new Crypto.AesKey(key, iv);
    }
    
    public static class AesKey {
        private byte[] key;
        private byte[] iv;

        public AesKey(byte[] key, byte[] iv) {
            this.key = key;
            this.iv = iv;
        }

        public byte[] getKey() {
            return key;
        }

        public void setKey(byte[] key) {
            this.key = key;
        }

        public byte[] getIv() {
            return iv;
        }

        public void setIv(byte[] iv) {
            this.iv = iv;
        }
    }
    
    public static class PublicKeyEncrypted {
        private byte[] encryptedAesKey;
        private byte[] encryptedData;

        public PublicKeyEncrypted(byte[] encryptedAesKey, byte[] encryptedData) {
            this.encryptedAesKey = encryptedAesKey;
            this.encryptedData = encryptedData;
        }

        public byte[] getEncryptedAesKey() {
            return encryptedAesKey;
        }

        public void setEncryptedAesKey(byte[] encryptedAesKey) {
            this.encryptedAesKey = encryptedAesKey;
        }

        public byte[] getEncryptedData() {
            return encryptedData;
        }

        public void setEncryptedData(byte[] encryptedData) {
            this.encryptedData = encryptedData;
        }
    }
    
    public static class EncryptedPrivateKey {
        private byte[] key;
        private byte[] iv;
        private byte[] salt;

        public EncryptedPrivateKey(byte[] key, byte[] iv, byte[] salt) {
            this.key = key;
            this.iv = iv;
            this.salt = salt;
        }

        public byte[] getKey() {
            return key;
        }

        public void setKey(byte[] key) {
            this.key = key;
        }

        public byte[] getIv() {
            return iv;
        }

        public void setIv(byte[] iv) {
            this.iv = iv;
        }

        public byte[] getSalt() {
            return salt;
        }

        public void setSalt(byte[] salt) {
            this.salt = salt;
        }
    }
}