package es.udc.fic.tic.nautilus.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.security.KeyPair;

import org.junit.After;
import org.junit.Test;

public class RSAManagerTest {
	
	private RSAManager manager = new RSAManager();
	
	@After
	public void cleanTestFiles() {
		new File("public.key").delete();
		new File("private.key").delete();
	}
	
	
	@Test
	public void createAndSaveKeysTest() {
		assertFalse(manager.existsPair());
		// Generate a key pair
		KeyPair pair = manager.generateKeys();
		
		// check if the keys are correctly save
		assertEquals(pair.getPublic(), manager.getPublicKey());
		assertEquals(pair.getPrivate(), manager.getPrivateKey());
		assertTrue(manager.existsPair());
	}
	
	@Test
	public void encryptAndDecryptInfomationTest() throws Exception {
		String personalInformation = "NR59XnWE6d8cnizbpVQNufEmrT1ROjcl4gORV77JL8c=";
		
		// Generate a key pair
		KeyPair pair = manager.generateKeys();
		
		// Encrypt
		String encryptMsg = manager.encrypt(personalInformation, pair.getPublic());
		
		// Decrypt
		String plainMsg = manager.decrypt(encryptMsg, pair.getPrivate());
		
		assertEquals(personalInformation, plainMsg);
	}
}
