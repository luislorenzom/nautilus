package es.udc.fic.tic.nautilus.config;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
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
		assertEquals(config.getLimitSpace(), 0);
		assertEquals(config.getLanguage(), ModelConstanst.LANGUAGE.EN);
		assertTrue(config.getServerPreferences().isEmpty());
	}
	
	@Test
	public void changeConfigurationTest() {
		// Get the file and modify
		File configFile = new File("config.xml");
		assertNotNull(configFile);
		ConfigHandler configHandler = new ConfigHandler();
		List<String> serverPreferences = new ArrayList<>();
		String server1 = "192.168.1.43";
		String server2 = "192.168.1.72";
		serverPreferences.add(server1);
		serverPreferences.add(server2);
		NautilusConfig newConfig = new NautilusConfig(true, 47815065, ModelConstanst.LANGUAGE.ES, serverPreferences, "~/nautilus_storage2");
		configHandler.changeConfig(newConfig);
		
		// Recovery the file and check the assertions
		NautilusConfig config = configHandler.getConfig();
		assertEquals(config.isServerAvailable(), true);
		assertEquals(config.getLimitSpace(), 47815065);
		assertEquals(config.getLanguage(), ModelConstanst.LANGUAGE.ES);
		assertEquals(config.getServerPreferences().size(), 2);
		
		assertEquals(config.getServerPreferences().get(0), "192.168.1.43");
		assertEquals(config.getServerPreferences().get(1), "192.168.1.72");
		
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
		assertEquals(config.getLimitSpace(), 0);
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
