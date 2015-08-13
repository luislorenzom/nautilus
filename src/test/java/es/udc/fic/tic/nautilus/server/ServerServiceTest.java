package es.udc.fic.tic.nautilus.server;

import static es.udc.fic.tic.nautilus.util.ModelConstanst.SPRING_CONFIG_FILE;
import static es.udc.fic.tic.nautilus.util.ModelConstanst.SPRING_CONFIG_TEST_FILE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import javax.management.InstanceNotFoundException;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import es.udc.fic.tic.nautilus.config.ConfigHandler;
import es.udc.fic.tic.nautilus.config.NautilusConfig;
import es.udc.fic.tic.nautilus.expcetion.FileUnavaliableException;
import es.udc.fic.tic.nautilus.expcetion.NotSaveException;
import es.udc.fic.tic.nautilus.expcetion.StorageLimitException;
import es.udc.fic.tic.nautilus.model.FileInfo;
import es.udc.fic.tic.nautilus.util.ModelConstanst;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { SPRING_CONFIG_FILE, SPRING_CONFIG_TEST_FILE })
@Transactional
public class ServerServiceTest {
	
	@Autowired
	private ServerService serverService;
	
	@BeforeClass
	public static void initializeStorageFolder() throws Exception {
		File folder = new File(System.getProperty("user.home")+"/nautilus_storage");
		folder.mkdirs();
		
		/* download a file to proof the storage limit */
		URL url = new URL("ftp://ftp.rediris.es/mirror/Apache/aurora/0.8.0/apache-aurora-0.8.0.tar.gz"); // 4.1Mb
		URLConnection connection = url.openConnection();
		
		InputStream in = connection.getInputStream();
		FileOutputStream out = new FileOutputStream(folder.getAbsoluteFile()+"/apache-aurora-0.8.0.tar.gz");
		
		byte[] data = new byte[1000];
		int read = in.read(data);
		
		while (read > 0) {
			out.write(data, 0, read);
			read = in.read(data);
		}
		
		in.close();
		out.close();
	}
	
	@AfterClass
	public static void deteleStorageFolder() {
		File storageFile = new File(System.getProperty("user.home")+"/nautilus_storage");
		deleteFolder(storageFile);
		storageFile.delete();
		
	}
	
	@Before
	public void createConfigFile() {
		ConfigHandler configHandler = new ConfigHandler();
		configHandler.initializeConfig();
	}
	
	@After
	public void deleteConfigFile() {
		new File("config.xml").delete();
	}
	
	@Test
	public void keepFileAllFieldsTest() throws NotSaveException, ParseException, StorageLimitException {
		FileInfo file = serverService.keepTheFile("~/nautilus/download1.aes", 3, 
				null, "17/12/2017 - 00:00:00", 
				123.33, "21a57f2fe765e1ae4a8bf15d73fc1bf2a533f547f2343d12a499d9c0592044d4");
		
		DateFormat df = new SimpleDateFormat("dd/MM/yyyy - HH:mm:ss");
		Calendar cal = Calendar.getInstance();
		cal.setTime(df.parse("17/12/2017 - 00:00:00"));
		
		assertEquals(file.getDownloadLimit(),3);
		assertEquals("Wrong max possible value for the size",123.33,
				file.getSize(), 0.01);
		assertEquals(file.getPath(), "~/nautilus/download1.aes");
		assertEquals(file.getDateLimit(), cal);
		assertEquals(file.getHash(), 
				"21a57f2fe765e1ae4a8bf15d73fc1bf2a533f547f2343d12a499d9c0592044d4");
	}
	
	@Test
	public void keepFileWithDownloadLimitNegativeTest() throws NotSaveException, ParseException, StorageLimitException {
		FileInfo file = serverService.keepTheFile("~/nautilus/download1.aes", -3, 
				null, "17/12/2017 - 00:00:00", 
				123.33, "21a57f2fe765e1ae4a8bf15d73fc1bf2a533f547f2343d12a499d9c0592044d4");
		
		assertEquals(file.getDownloadLimit(), -1);
	}
	
	@Test
	public void keepFileWithoutDateLimitTest() throws NotSaveException, ParseException, StorageLimitException {
		FileInfo file = serverService.keepTheFile("~/nautilus/download1.aes", 3, 
				null, null, 123.33, "21a57f2fe765e1ae4a8bf15d73fc1bf2a533f547f2343d12a499d9c0592044d4");
		
		assertNull(file.getDateLimit());
	}
	
	@Test
	public void getFileByHashWithoutDateLimitAndDownloadLimitTest() 
			throws NotSaveException, ParseException, InstanceNotFoundException, FileUnavaliableException, StorageLimitException {
		
		FileInfo file = serverService.keepTheFile("~/nautilus/download1.aes", -1, 
				null, null, 123.33, "21a57f2fe765e1ae4a8bf15d73fc1bf2a533f547f2343d12a499d9c0592044d4");
		FileInfo fileRecovered = serverService.returnFile("21a57f2fe765e1ae4a8bf15"
				+ "d73fc1bf2a533f547f2343d12a499d9c0592044d4");
		
		assertEquals(file, fileRecovered);
	}
	
	@Test(expected=IndexOutOfBoundsException.class)
	public void getFileByHashInstanceNotFoundTest()
			throws NotSaveException, ParseException, InstanceNotFoundException, FileUnavaliableException {
	
		serverService.returnFile("21a57f2fe765e1ae4a8bf15d73fc1bf2a533f547f2343"
				+ "d12a499d9c0592044d4");
	}
	
	@Test
	public void decrementDownloadLimitTest() throws NotSaveException, ParseException, 
	InstanceNotFoundException, FileUnavaliableException, StorageLimitException {
		
		String hash = "21a57f2fe765e1ae4a8bf15d73fc1bf2a533f547f2343d12a499d9c0592044d4";
		FileInfo file = serverService.keepTheFile("~/nautilus/download1.aes", 2, 
				null, null, 123.33, hash);
		
		assertEquals(file.getDownloadLimit(), 2);
		serverService.returnFile(hash);
		assertEquals(file.getDownloadLimit(), 1);
		FileInfo tmpFile = serverService.returnFile(hash);
		assertEquals(file, tmpFile);
		assertEquals(file.getDownloadLimit(), 0);
		FileInfo finalFile = serverService.returnFile(hash);
		assertNull(finalFile);
	}
	
	@Test
	public void notDecrementDownloadLimitTest() throws NotSaveException, ParseException, 
	InstanceNotFoundException, FileUnavaliableException, StorageLimitException {
		
		String hash = "21a57f2fe765e1ae4a8bf15d73fc1bf2a533f547f2343d12a499d9c0592044d4";
		FileInfo file = serverService.keepTheFile("~/nautilus/download1.aes", 0, 
				null, null, 123.33, hash);
		
		serverService.returnFile(hash);
		assertEquals(file.getDownloadLimit(), -1);
		FileInfo finalFile = serverService.returnFile(hash);
		assertEquals(file.getDownloadLimit(), -1);
		assertEquals(file, finalFile);
	}
	
	@Test
	public void underDateLimitTest() throws NotSaveException, ParseException, 
	InstanceNotFoundException, FileUnavaliableException, StorageLimitException {
		
		String hash = "21a57f2fe765e1ae4a8bf15d73fc1bf2a533f547f2343d12a499d9c0592044d4";
		serverService.keepTheFile("~/nautilus/download1.aes", 0, 
				null, "12/02/2015 - 00:00:00", 123.33, hash);
		
		FileInfo  file = serverService.returnFile(hash);
		assertNull(file);
	}
	
	@Test
	public void highDateLimitTest() throws NotSaveException, ParseException, 
	InstanceNotFoundException, FileUnavaliableException, StorageLimitException {
		
		String hash = "21a57f2fe765e1ae4a8bf15d73fc1bf2a533f547f2343d12a499d9c0592044d4";
		FileInfo initialFile = serverService.keepTheFile("~/nautilus/download1.aes", 0, 
				null, "12/12/2018 - 00:00:00", 123.33, hash);
		
		FileInfo file = serverService.returnFile(hash);
		assertEquals(initialFile, file);
	}
	
	@Test(expected=FileUnavaliableException.class)
	public void underReleaseDateTest() throws NotSaveException, ParseException,
		InstanceNotFoundException, FileUnavaliableException, StorageLimitException {
		String hash = "21a57f2fe765e1ae4a8bf15d73fc1bf2a533f547f2343d12a499d9c0592044d4";
		serverService.keepTheFile("~/nautilus/download1.aes", 0, 
				"12/12/2016 - 00:00:00", "12/12/2018 - 00:00:00", 123.33, hash);
		
		serverService.returnFile(hash);
	}
	
	@Test
	public void onTheRealeaseDateTest() throws NotSaveException, ParseException
		, InstanceNotFoundException, FileUnavaliableException, StorageLimitException {
		String hash = "21a57f2fe765e1ae4a8bf15d73fc1bf2a533f547f2343d12a499d9c0592044d4";
		serverService.keepTheFile("~/nautilus/download1.aes", 0, 
				"12/04/2015 - 00:00:00", "12/12/2018 - 00:00:00", 123.33, hash);
		FileInfo file = serverService.returnFile(hash);
		
		assertEquals(file.getHash(), hash);
		assertEquals(file.getPath(), "~/nautilus/download1.aes");
	}
	
	@Test
	public void underStorageLimit() throws Exception {
		// Change the config parameter to add the storage limits
		ConfigHandler configHandler = new ConfigHandler();
		NautilusConfig newConfig = new NautilusConfig(true, 7.1F, ModelConstanst.LANGUAGE.ES, null, 
				System.getProperty("user.home")+"/nautilus_storage");
		configHandler.changeConfig(newConfig);
		
		String hash = "21a57f2fe765e1ae4a8bf15d73fc1bf2a533f547f2343d12a499d9c0592044d4";
		serverService.keepTheFile("~/nautilus_storage/download1.aes", 0, null, null, 1.3, hash);
		
		FileInfo fileSaved = serverService.returnFile(hash);
		
		assertEquals(fileSaved.getHash(), hash);
	}
	
	@Test(expected=StorageLimitException.class)
	public void onStorageLimit() throws Exception {
		// Change the config parameter to add the storage limits
		ConfigHandler configHandler = new ConfigHandler();
		NautilusConfig newConfig = new NautilusConfig(true, 7.1F, ModelConstanst.LANGUAGE.ES, null, 
				System.getProperty("user.home")+"/nautilus_storage");
		configHandler.changeConfig(newConfig);
		
		String hash = "21a57f2fe765e1ae4a8bf15d73fc1bf2a533f547f2343d12a499d9c0592044d4";
		serverService.keepTheFile("~/nautilus_storage/download1.aes", 0, null, null, 4.3, hash);
		
		FileInfo fileSaved = serverService.returnFile(hash);
		
		assertEquals(fileSaved.getHash(), hash);
	}
	
	/*********************
	 * Private functions *
	 *********************/
	
	private static void deleteFolder(File rootFolder) {
		File[] files = rootFolder.listFiles();
		
		for (int x = 0; x < files.length; x++) {
			if (files[x].isDirectory()) {
				deleteFolder(files[x]);
			} else {
				files[x].delete();
			}
		}
	}
}
