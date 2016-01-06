package com.example.scanner;

import java.security.Key;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import android.util.Base64;
import android.util.Log;

public class EncryptionFunctions {

	static String encrypt(String plainData)
	{
		String encrypted = null;
		try {
			KeyGenerator keyGen = KeyGenerator.getInstance("AES"); 
			keyGen.init(128); 
			SecretKey secretKey = keyGen.generateKey(); 
			Cipher aesCipher = Cipher.getInstance("AES");
			aesCipher.init(Cipher.ENCRYPT_MODE,secretKey); 
			byte[] byteCipherText = aesCipher.doFinal(plainData.getBytes()); 
			encrypted =  Base64.encodeToString(byteCipherText, Base64.DEFAULT);
			AddAccount.stringKey = Base64.encodeToString(secretKey.getEncoded(), Base64.DEFAULT);
			return encrypted;
		}
		catch(Exception e) { } 
		return encrypted;
	}	
	
	static String encrypt(String plainData, String key)
	{
		String encrypted = null;
		if (plainData == null)
		{
			Log.w("Exception", "Strig was empty");
		}
		else{
			try{
				byte[] encodedKey = Base64.decode(key, Base64.DEFAULT);
				Key secretKey = new SecretKeySpec(encodedKey,0,encodedKey.length, "AES");  
				Cipher aesCipher = Cipher.getInstance("AES");
				aesCipher.init(Cipher.ENCRYPT_MODE, secretKey); 
				byte[] byteCipherText = aesCipher.doFinal(plainData.getBytes()); 
				encrypted =  Base64.encodeToString(byteCipherText, Base64.DEFAULT);
				return encrypted;	
			}catch(Exception e)
			{
				Log.w("Exception", e.toString());
			}
		}
		return encrypted;

	}

	static String decrypt(String encryptedText, String key)
	{
		String decrypted = null;
		try{
			byte[] encodedKey = Base64.decode(key, Base64.DEFAULT);
			Key secretKey = new SecretKeySpec(encodedKey,0,encodedKey.length, "AES");  
			Cipher cipher = Cipher.getInstance("AES");
			cipher.init(Cipher.DECRYPT_MODE, secretKey);
			byte[] encryptedTextBytes = Base64.decode(encryptedText, Base64.DEFAULT);
			byte[] decryptedTextBytes = cipher.doFinal(encryptedTextBytes);
			decrypted = new String(decryptedTextBytes);
			return decrypted;
		}
		catch(Exception e)
		{
			Log.w("Exception", e.toString());
		}
		return decrypted;
	}
}
