package es.udc.fic.tic.nautilus.config;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.junit.AfterClass;
import org.junit.Test;

import es.udc.fic.tic.nautilus.util.ModelConstanst;

public class ConfigHandlerTest {
	
	@AfterClass
	public static void deleteConfigFile() {
		// Delete the config file generated in this test
		File configFile = new File("config.xml");
		configFile.delete();
	}
	
	@Test
	public void initializeConfigTest() {
		ConfigHandler configHandler = new ConfigHandler();
		configHandler.initializeConfig();
		NautilusConfig config = configHandler.getConfig();
		
		// Check with default configuration
		assertEquals(config.isServerAvailable(), false);
		assertNull(config.getLimitSpace());
		assertEquals(config.getLanguage(), ModelConstanst.LANGUAGE.EN);
		assertTrue(config.getServerPreferences().isEmpty());
	}
	
	@Test
	public void changeConfigurationTest() {
		// Get the file and modify
		File configFile = new File("config.xml");
		assertNotNull(configFile);
		ConfigHandler configHandler = new ConfigHandler();
		List<ServerInfo> serverPreferences = new ArrayList<>();
		ServerInfo server1 = new ServerInfo("name1", "hash1");
		ServerInfo server2 = new ServerInfo("name2", "hash2");
		serverPreferences.add(server1);
		serverPreferences.add(server2);
		NautilusConfig newConfig = new NautilusConfig(true, 45.6F, ModelConstanst.LANGUAGE.ES, serverPreferences, "~/nautilus_storage2");
		configHandler.changeConfig(newConfig);
		
		// Recovery the file and check the assertions
		NautilusConfig config = configHandler.getConfig();
		assertEquals(config.isServerAvailable(), true);
		assertEquals("Wrong max possible value for the size",45.6F,
				config.getLimitSpace(), 0.01);
		assertEquals(config.getLanguage(), ModelConstanst.LANGUAGE.ES);
		assertEquals(config.getServerPreferences().size(), 2);
		
		
		assertEquals(config.getServerPreferences().get(0).getName(), "name1");
		assertEquals(config.getServerPreferences().get(0).getHash(), "hash1");
		assertEquals(config.getServerPreferences().get(1).getName(), "name2");
		assertEquals(config.getServerPreferences().get(1).getHash(), "hash2");
		
		assertEquals(config.getStorageFolder(), "~/nautilus_storage2");
	}
	
	@Test
	public void restoreConfiguracionTest() {
		// Restore the config file modify in the second test
		ConfigHandler configHandler = new ConfigHandler();
		configHandler.restoreConfig();
		NautilusConfig config = configHandler.getConfig();
		
		// Check the assertions
		assertEquals(config.isServerAvailable(), false);
		assertNull(config.getLimitSpace());
		assertEquals(config.getLanguage(), ModelConstanst.LANGUAGE.EN);
		assertTrue(config.getServerPreferences().isEmpty());
		
		if (System.getProperty("os.name").contains("win")) {
			// Windows system
			assertEquals(config.getStorageFolder(), System.getProperty("user.home") + "\nautilus_storage");
		} else {
			// Unix system
			assertEquals(config.getStorageFolder(), System.getProperty("user.home") + "/nautilus_storage");
		}
	}
}
