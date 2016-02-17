package es.udc.fic.tic.nautilus.util;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.PrintWriter;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

import javax.crypto.Cipher;

import org.springframework.util.Base64Utils;

public class RSAManager {
	
	/**
	 * This function generate two files with public and private key
	 * 
	 * @return KeyPair The key pair
	 */
	public KeyPair generateKeys () {
		KeyPair pair = null;
		try {
			// Generate Keys
			KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
			keyGen.initialize(4096);
			KeyPair key = keyGen.generateKeyPair();
			
			// Save the public key
			String stringPublicKey = Base64Utils.encodeToString(key.getPublic().getEncoded());
			PrintWriter out = new PrintWriter("public.key");
			out.println(stringPublicKey);
			out.close();
			
			// Save the private key
			String stringPrivateKey = Base64Utils.encodeToString(key.getPrivate().getEncoded());
			out = new PrintWriter("private.key");
			out.println(stringPrivateKey);
			out.close();
			pair = key;
			
		} catch (Exception e) {
			System.err.println("Has been happened one error in the key pair generation");
		}
		return pair;
	}
	
	/**
	 * Get the the public key save in the file public.key
	 * 
	 * @return PublicKey The public key save in the program folder
	 */
	public PublicKey getPublicKey () {
		PublicKey key = null;
		try {
			// Open the file and read the bytes
			File publicKeyFile = new File("public.key");
	        FileInputStream fis = new FileInputStream(publicKeyFile);
	        DataInputStream dis = new DataInputStream(fis);
	        byte[] keyBytes = new byte[(int) publicKeyFile.length()];
	        dis.readFully(keyBytes);
	        dis.close();
	        
	        // Decode the bytes
	        String tmp = new String(keyBytes);
	        byte[] tmpDecoded = Base64Utils.decodeFromString(tmp);
	        
	        // Transform the bytes into PublicKey
	        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(tmpDecoded);
	        KeyFactory kf = KeyFactory.getInstance("RSA");
	        key = kf.generatePublic(keySpec);
	        
		} catch (Exception e) {
			System.err.println("Can't recovery the public key");
		}
		return key;
	}
	
	/**
	 * Get the the private key save in the file private.key
	 * 
	 * @return PrivateKey The private key save in the program folder
	 */
	public PrivateKey getPrivateKey () {
		PrivateKey key = null;
		try {
			// Open the file and read the bytes
			File privateKeyFile = new File("private.key");
	        FileInputStream fis = new FileInputStream(privateKeyFile);
	        DataInputStream dis = new DataInputStream(fis);
	        byte[] keyBytes = new byte[(int) privateKeyFile.length()];
	        dis.readFully(keyBytes);
	        dis.close();
	        
	        // Decode the bytes
	        String tmp = new String(keyBytes);
	        byte[] tmpDecoded = Base64Utils.decodeFromString(tmp);
	        
	        // Transform the bytes into PrivateKey
	        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(tmpDecoded);
	        KeyFactory kf = KeyFactory.getInstance("RSA");
	        key = kf.generatePrivate(keySpec);
	        
			
		} catch (Exception e) {
			System.err.println("Can't recovery the private key");
		}
		return key;
	}
	
	/**
	 * This function encrypt the information that you will give
	 * 
	 * @param String the plain string that you want to encrypt
	 * @param PublicKey the public for encrypt the message
	 * @return String the plain string encrypted
	 */
	public String encrypt (String plainString, PublicKey key) {
		String encryptMessage = null;
		try {
			// Initialized the cipher in encrypt mode
			Cipher cipher = Cipher.getInstance("RSA");
			cipher.init(Cipher.ENCRYPT_MODE, key);
			
			// Encrypt the message
			byte[] data = cipher.doFinal(plainString.getBytes());
			encryptMessage = Base64Utils.encodeToString(data);
			
		} catch (Exception e) {
			System.err.println("Can't encrypt the information");
		}
		return encryptMessage;
	}
	
	/**
	 * This function decrypt the information that you will give
	 * 
	 * @param String the encrypt string that want to recovery
	 * @param PrivateKey the private for decrypt the message
	 * @return String the decrypt string 
	 */
	public String decrypt (String encryptString, PrivateKey key) {
		String plainMessage = null;
		try {
			// Initialized the cipher in encrypt mode
			Cipher cipher = Cipher.getInstance("RSA");
			cipher.init(Cipher.DECRYPT_MODE, key);
			
			// Decrypt the message
			byte[] data = cipher.doFinal(Base64Utils.decode(encryptString.getBytes()));
			plainMessage = new String (data);
			
		} catch (Exception e) {
			System.err.println("Can't decrypt the information");
		}
		return plainMessage;
	}
}
