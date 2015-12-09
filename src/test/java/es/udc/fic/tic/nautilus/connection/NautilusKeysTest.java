package es.udc.fic.tic.nautilus.connection;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

import net.tomp2p.peers.Number160;

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
		Number160 host = Number160.createHash("p1");
		Number160 hostBackup = Number160.createHash("p2");
		SecretKey key = keyGen.generateKey();
		
		NautilusKey nkey = new NautilusKey(fileName, key, host, hostBackup);
		lkeys.add(nkey);
		
		handler.generateKeys(lkeys);
		assertNotNull("key.xml");
		
		List<NautilusKey> listRecovery = handler.getKeys("key.xml");
		assertEquals(listRecovery.size(), 1);
		
		NautilusKey recoveryKey = listRecovery.get(0);
		
		assertEquals(recoveryKey.getKey(), nkey.getKey());
		assertEquals(recoveryKey.getHost(), nkey.getHost());
		assertEquals(recoveryKey.getHostBackup(), nkey.getHostBackup());
		assertEquals(recoveryKey.getFileName(), nkey.getFileName());
	}
}
