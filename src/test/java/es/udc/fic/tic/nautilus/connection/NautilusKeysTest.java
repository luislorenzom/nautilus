package es.udc.fic.tic.nautilus.connection;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.junit.AfterClass;
import org.junit.Test;
import org.springframework.util.Base64Utils;

import es.udc.fic.tic.nautilus.util.ModelConstanst.ENCRYPT_ALG;

public class NautilusKeysTest {
	
	@AfterClass
	public static void deleteKey() {
		new File("hola__key.xml").delete();
	}
	
	@Test
	public void generateKeyTest() throws Exception {
		NautilusKeysHandler handler = new NautilusKeysHandler();
		
		List<NautilusKey> lkeys = new ArrayList<NautilusKey>();
		
		KeyGenerator keyGen = KeyGenerator.getInstance("AES");
		keyGen.init(256);
		
		String fileName = "hola_mundo.txt";
		String hash = "dlkfsdjlfkjsdlfkjiowejpemfwcm23n42fjskvdfdsf";
		String host = "192.168.1.76";
		String hostBackup = "192.168.1.54";
		SecretKey secretKey = keyGen.generateKey();
		String key = Base64Utils.encodeToString(secretKey.getEncoded());
		
		NautilusKey nkey = new NautilusKey(fileName, key, ENCRYPT_ALG.AES, hash, host, hostBackup);
		lkeys.add(nkey);
		
		handler.generateKeys(lkeys);
		assertNotNull(new File("hola__key.xml"));
		
		List<NautilusKey> listRecovery = handler.getKeys("hola__key.xml");
		assertEquals(listRecovery.size(), 1);
		
		NautilusKey recoveryKey = listRecovery.get(0);
		
		assertEquals(stringToSecretKey(recoveryKey.getKey()),stringToSecretKey(nkey.getKey()));
		assertEquals(recoveryKey.getHash(), nkey.getHash());
		assertEquals(recoveryKey.getHost(), nkey.getHost());
		assertEquals(recoveryKey.getHostBackup(), nkey.getHostBackup());
		assertEquals(recoveryKey.getFileName(), nkey.getFileName());
		assertEquals(recoveryKey.getEncryptAlg(), ENCRYPT_ALG.AES);
	}
	
	/*********************/
	/* Private functions */
	/*********************/
	private SecretKey stringToSecretKey (String stringKey) {		
		return new SecretKeySpec(Base64Utils.decodeFromString(stringKey), "AES");
	}
}
