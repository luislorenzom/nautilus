package es.udc.fic.tic.nautilus.server;

import static es.udc.fic.tic.nautilus.util.ModelConstanst.SPRING_CONFIG_FILE;
import static es.udc.fic.tic.nautilus.util.ModelConstanst.SPRING_CONFIG_TEST_FILE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

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
		
		DateFormat df_0 = new SimpleDateFormat("dd/MM/yyyy - HH:mm:ss");
		Calendar cal_0 = Calendar.getInstance();
		cal_0.setTime(df_0.parse("17/12/2017 - 00:00:00"));
		
		FileInfo file = serverService.keepTheFile("~/nautilus/download1.aes", 3, 
				null, cal_0, 
				12333, "21a57f2fe765e1ae4a8bf15d73fc1bf2a533f547f2343d12a499d9c0592044d4");
		
		DateFormat df = new SimpleDateFormat("dd/MM/yyyy - HH:mm:ss");
		Calendar cal = Calendar.getInstance();
		cal.setTime(df.parse("17/12/2017 - 00:00:00"));
		
		assertEquals(file.getDownloadLimit(),3);
		assertEquals(file.getSize(), 12333);
		assertEquals(file.getPath(), "~/nautilus/download1.aes");
		assertEquals(file.getDateLimit(), cal);
		assertEquals(file.getHash(), 
				"21a57f2fe765e1ae4a8bf15d73fc1bf2a533f547f2343d12a499d9c0592044d4");
	}
	
	@Test
	public void keepFileWithDownloadLimitNegativeTest() throws NotSaveException, ParseException, StorageLimitException {
		DateFormat df_0 = new SimpleDateFormat("dd/MM/yyyy - HH:mm:ss");
		Calendar cal_0 = Calendar.getInstance();
		cal_0.setTime(df_0.parse("17/12/2017 - 00:00:00"));
		
		FileInfo file = serverService.keepTheFile("~/nautilus/download1.aes", -3, 
				null, cal_0, 
				12333, "21a57f2fe765e1ae4a8bf15d73fc1bf2a533f547f2343d12a499d9c0592044d4");
		
		assertEquals(file.getDownloadLimit(), -1);
	}
	
	@Test
	public void keepFileWithoutDateLimitTest() throws NotSaveException, ParseException, StorageLimitException {
		FileInfo file = serverService.keepTheFile("~/nautilus/download1.aes", 3, 
				null, null, 12333, "21a57f2fe765e1ae4a8bf15d73fc1bf2a533f547f2343d12a499d9c0592044d4");
		
		assertNull(file.getDateLimit());
	}
	
	@Test
	public void getFileByHashWithoutDateLimitAndDownloadLimitTest() 
			throws NotSaveException, ParseException, InstanceNotFoundException, FileUnavaliableException, StorageLimitException {
		
		FileInfo file = serverService.keepTheFile("~/nautilus/download1.aes", -1, 
				null, null, 12333, "21a57f2fe765e1ae4a8bf15d73fc1bf2a533f547f2343d12a499d9c0592044d4");
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
				null, null, 12333, hash);
		
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
				null, null, 12333, hash);
		
		serverService.returnFile(hash);
		assertEquals(file.getDownloadLimit(), -1);
		FileInfo finalFile = serverService.returnFile(hash);
		assertEquals(file.getDownloadLimit(), -1);
		assertEquals(file, finalFile);
	}
	
	@Test
	public void underDateLimitTest() throws NotSaveException, ParseException, 
	InstanceNotFoundException, FileUnavaliableException, StorageLimitException {
		
		DateFormat df_0 = new SimpleDateFormat("dd/MM/yyyy - HH:mm:ss");
		Calendar cal_0 = Calendar.getInstance();
		cal_0.setTime(df_0.parse("12/02/2015 - 00:00:00"));
		
		String hash = "21a57f2fe765e1ae4a8bf15d73fc1bf2a533f547f2343d12a499d9c0592044d4";
		serverService.keepTheFile("~/nautilus/download1.aes", 0, 
				null, cal_0, 12333, hash);
		
		FileInfo  file = serverService.returnFile(hash);
		assertNull(file);
	}
	
	@Test
	public void highDateLimitTest() throws NotSaveException, ParseException, 
	InstanceNotFoundException, FileUnavaliableException, StorageLimitException {
		
		DateFormat df_0 = new SimpleDateFormat("dd/MM/yyyy - HH:mm:ss");
		Calendar cal_0 = Calendar.getInstance();
		cal_0.setTime(df_0.parse("12/12/2018 - 00:00:00"));
		
		String hash = "21a57f2fe765e1ae4a8bf15d73fc1bf2a533f547f2343d12a499d9c0592044d4";
		FileInfo initialFile = serverService.keepTheFile("~/nautilus/download1.aes", 0, 
				null, cal_0, 12333, hash);
		
		FileInfo file = serverService.returnFile(hash);
		assertEquals(initialFile, file);
	}
	
	@Test(expected=FileUnavaliableException.class)
	public void underReleaseDateTest() throws NotSaveException, ParseException,
		InstanceNotFoundException, FileUnavaliableException, StorageLimitException {
		
		DateFormat df_0 = new SimpleDateFormat("dd/MM/yyyy - HH:mm:ss");
		Calendar cal_0 = Calendar.getInstance();
		cal_0.setTime(df_0.parse("12/12/2016 - 00:00:00"));
		
		DateFormat df_1 = new SimpleDateFormat("dd/MM/yyyy - HH:mm:ss");
		Calendar cal_1 = Calendar.getInstance();
		cal_1.setTime(df_1.parse("12/12/2018 - 00:00:00"));
		
		String hash = "21a57f2fe765e1ae4a8bf15d73fc1bf2a533f547f2343d12a499d9c0592044d4";
		serverService.keepTheFile("~/nautilus/download1.aes", 0, 
				cal_0, cal_1, 12333, hash);
		
		serverService.returnFile(hash);
	}
	
	@Test
	public void onTheRealeaseDateTest() throws NotSaveException, ParseException
		, InstanceNotFoundException, FileUnavaliableException, StorageLimitException {
		
		DateFormat df_0 = new SimpleDateFormat("dd/MM/yyyy - HH:mm:ss");
		Calendar cal_0 = Calendar.getInstance();
		cal_0.setTime(df_0.parse("12/04/2015 - 00:00:00"));
		
		DateFormat df_1 = new SimpleDateFormat("dd/MM/yyyy - HH:mm:ss");
		Calendar cal_1 = Calendar.getInstance();
		cal_1.setTime(df_1.parse("12/12/2018 - 00:00:00"));
		
		String hash = "21a57f2fe765e1ae4a8bf15d73fc1bf2a533f547f2343d12a499d9c0592044d4";
		
		serverService.keepTheFile("~/nautilus/download1.aes", 0, 
				cal_0, cal_1, 12333, hash);
		FileInfo file = serverService.returnFile(hash);
		
		assertEquals(file.getHash(), hash);
		assertEquals(file.getPath(), "~/nautilus/download1.aes");
	}
	
	@Test
	public void allowSaveFileTest() throws Exception {
		ConfigHandler configHandler = new ConfigHandler();
		NautilusConfig newConfig = configHandler.getConfig();
		
		newConfig.setServerAvailable(true);
		newConfig.setLimitSpace(1000L);
		configHandler.changeConfig(newConfig);
		
		String hash = "21a57f2fe765e1ae4a8bf15d73fc1bf2a533f547f2343d12a499d9c0592044d4";
		serverService.keepTheFile("~/nautilus/download1.aes", 0, 
				null, null, 333, hash);
		
		boolean result = serverService.checkFileSize(666L);
		assertTrue(result);
	}
	
	@Test
	public void denySaveFileTest() throws Exception {
		ConfigHandler configHandler = new ConfigHandler();
		NautilusConfig newConfig = configHandler.getConfig();
		
		newConfig.setServerAvailable(true);
		newConfig.setLimitSpace(1000L);
		configHandler.changeConfig(newConfig);
		
		String hash = "21a57f2fe765e1ae4a8bf15d73fc1bf2a533f547f2343d12a499d9c0592044d4";
		serverService.keepTheFile("~/nautilus/download1.aes", 0, 
				null, null, 333, hash);
	
		String hash2 = "21a57f2fe735e1ae4a9bf15d33fc1af2a533f547f2343d12a499d9c0592044d4";
		serverService.keepTheFile("~/nautilus/download2.aes", 0, 
				null, null, 666, hash2);
		
		boolean result = serverService.checkFileSize(2L);
		assertFalse(result);
	}
	
	@Test(expected=IndexOutOfBoundsException.class)
	public void deleteAllexpiratedFilesTest() throws Exception {
		ConfigHandler configHandler = new ConfigHandler();
		NautilusConfig newConfig = configHandler.getConfig();
		
		newConfig.setServerAvailable(true);
		newConfig.setLimitSpace(-1L);
		configHandler.changeConfig(newConfig);
		
		String hash1 = "21a57f2fe735e1ae4a9bf15d33fc1af2a533f547f2343d12a499d9c0592044d4";
		String hash2 = "22a57f2fe735e1ae4a9bf15d33fc1af2a533f547f2343d12a499d9c0592044d4";
		String hash3 = "23a57f2fe735e1ae4a9bf15d33fc1af2a533f547f2343d12a499d9c0592044d4";
		String hash4 = "24a57f2fe735e1ae4a9bf15d33fc1af2a533f547f2343d12a499d9c0592044d4";
		
		DateFormat df = new SimpleDateFormat("dd/MM/yyyy - HH:mm:ss");
		
		Calendar date1 = Calendar.getInstance();
		date1.setTime(df.parse("12/04/2015 - 00:00:00"));
		Calendar date2 = Calendar.getInstance();
		date2.setTime(df.parse("17/11/2017 - 00:00:00"));
		Calendar date3 = Calendar.getInstance();
		date3.setTime(df.parse("11/01/2016 - 00:00:00"));
		Calendar date4 = Calendar.getInstance();
		date4.setTime(df.parse("12/01/2017 - 00:00:00"));
		
		String path = "~/nautilus_storage/";
		
		serverService.keepTheFile(path+"file1", 0, null, date1, 1991L, hash1);
		serverService.keepTheFile(path+"file2", 0, null, date2, 1992L, hash2);
		serverService.keepTheFile(path+"file3", 0, null, date3, 1993L, hash3);
		serverService.keepTheFile(path+"file4", 0, null, date4, 1994L, hash4);
		
		serverService.deleteAllExpiratedFiles();
		
		FileInfo f1 = serverService.returnFile(hash2);
		assertEquals(f1.getDateLimit(), date2);
		FileInfo f2 = serverService.returnFile(hash4);
		assertEquals(f2.getDateLimit(), date4);
		
		serverService.returnFile(hash1);
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
