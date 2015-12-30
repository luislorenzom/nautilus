package es.udc.fic.tic.nautilus.connection;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

import org.junit.AfterClass;
import org.junit.Test;

public class NautilusKeysTest {
	
	@AfterClass
	public static void deleteKey() {
		new File("key.xml").delete();
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
		SecretKey key = keyGen.generateKey();
		
		NautilusKey nkey = new NautilusKey(fileName, key, hash, host, hostBackup);
		lkeys.add(nkey);
		
		handler.generateKeys(lkeys);
		assertNotNull("key.xml");
		
		List<NautilusKey> listRecovery = handler.getKeys("key.xml");
		assertEquals(listRecovery.size(), 1);
		
		NautilusKey recoveryKey = listRecovery.get(0);
		
		assertEquals(recoveryKey.getKey(), nkey.getKey());
		assertEquals(recoveryKey.getHash(), nkey.getHash());
		assertEquals(recoveryKey.getHost(), nkey.getHost());
		assertEquals(recoveryKey.getHostBackup(), nkey.getHostBackup());
		assertEquals(recoveryKey.getFileName(), nkey.getFileName());
	}
}
