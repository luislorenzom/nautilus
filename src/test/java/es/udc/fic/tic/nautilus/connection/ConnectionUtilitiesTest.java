package es.udc.fic.tic.nautilus.connection;

import static es.udc.fic.tic.nautilus.util.ModelConstanst.SPRING_CONFIG_FILE;
import static es.udc.fic.tic.nautilus.util.ModelConstanst.SPRING_CONFIG_TEST_FILE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import es.udc.fic.tic.nautilus.config.ConfigHandler;
import es.udc.fic.tic.nautilus.config.NautilusConfig;
import es.udc.fic.tic.nautilus.server.ServerService;
import es.udc.fic.tic.nautilus.util.ModelConstanst.LANGUAGE;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { SPRING_CONFIG_FILE, SPRING_CONFIG_TEST_FILE })
@Transactional
public class ConnectionUtilitiesTest {
	
	@Autowired
	private ConnectionUtilities connectionUtilities;
	
	@Autowired
	private ServerService serverService;
	
	//@BeforeClass
	public static void initializeStorageFolderAndGenerateConfigFile() throws Exception {
		/* Initialize the storage folder and one file */
		File folder = new File(System.getProperty("user.home")+"/nautilus_storage");
		folder.mkdirs();
		
		String uri = "ftp://ftp.rediris.es/mirror/Apache/aurora/0.8.0/apache-aurora-0.8.0.tar.gz";
		
		/* download a file */
		downloadFileByUrl(uri, folder);
		
		/* Now generate a config.xml */
		ConfigHandler configHandler = new ConfigHandler();
		NautilusConfig config = new NautilusConfig(true, 10485760, LANGUAGE.EN, null, 
				System.getProperty("user.home") + "/nautilus_storage");
		configHandler.changeConfig(config);
	}
	
	//@AfterClass
	public static void deteleStorageFolderAndConfigFile() {
		/* Delete storage folder */
		File storageFile = new File(System.getProperty("user.home")+"/nautilus_storage");
		deleteFolder(storageFile);
		storageFile.delete();
		
		/* Delete configuration file */
		new File("config.xml").delete();
		
		/* Delete a file */
		new File("apache-aurora-0.8.0.tar.gz").delete();	
	}
	
	
	//@Test
	public void procesingMessageTypeOneUnderLimitTest() throws Exception {
		/* save the file */
		String uri = "ftp://ftp.rediris.es/mirror/Apache/aurora/0.10.0/apache-aurora-0.10.0.tar.gz";
		downloadFileByUrl(uri, new File("."));
		File downloadFile = new File("apache-aurora-0.10.0.tar.gz");
		
		NautilusMessage msg = new NautilusMessage(1, "file1", readContentIntoByteArray(downloadFile), 
				0, null, null);
		
		int result = connectionUtilities.processMessageTypeOne(msg);
		assertEquals(result, 1);
	}
	
	//@Test
	public void procesingMessageTypeOneOverLimitTest() throws Exception {
		String uri = "ftp://ftp.rediris.es/mirror/Apache/aurora/0.9.0/apache-aurora-0.9.0.tar.gz";
		downloadFileByUrl(uri, new File("."));
		File downloadFile = new File("apache-aurora-0.9.0.tar.gz");
		
		NautilusMessage msg = new NautilusMessage(1, "file2", readContentIntoByteArray(downloadFile), 
				0, null, null);
		
		int result = connectionUtilities.processMessageTypeOne(msg);
		assertEquals(result, -1);
	}
	
	//@Test
	public void procesingMessageTypeOneWithoutPermissionTest() throws Exception {
		/* Change the config.xml */
		ConfigHandler configHandler = new ConfigHandler();
		NautilusConfig config = new NautilusConfig(false, 0, LANGUAGE.EN, 
				null, System.getProperty("user.home")+"/nautilus_storage");
		configHandler.changeConfig(config);
		
		String uri = "ftp://ftp.rediris.es/mirror/Apache/aurora/0.9.0/apache-aurora-0.9.0.tar.gz";
		downloadFileByUrl(uri, new File("."));
		File downloadFile = new File("apache-aurora-0.9.0.tar.gz");
		
		NautilusMessage msg = new NautilusMessage(1, "file2", readContentIntoByteArray(downloadFile), 
				0, null, null);
		
		int result = connectionUtilities.processMessageTypeOne(msg);
		assertEquals(result, -1);
	}
	
	//@Test
	public void procesingMessageTypeZeroSuccessTest() throws Exception {
		/* save the file 0 in the system */
		File file= new File(System.getProperty("user.home") + 
				"/nautilus_storage/apache-aurora-0.8.0.tar.gz");
		serverService.keepTheFile(file.getAbsolutePath(), 0, null, null, file.length(), "file0");
		
		NautilusMessage msg = new NautilusMessage(0, "file0");
		byte[] result = connectionUtilities.processMessageTypeZero(msg);
		
		assertEquals(result.length, file.length());
	}
	
	//@Test
	public void procesingMessageTypeZeroNotFoundTest() throws Exception {
		NautilusMessage msg = new NautilusMessage(0, "file_not_saved");
		byte[] result = connectionUtilities.processMessageTypeZero(msg);
		
		assertNull(result);
	}
	
	//@Test
	public void procesingMessageTypeZeroNotReleasedTest() throws Exception {
		/* save the file 0 in the system */
		
		DateFormat df = new SimpleDateFormat("dd/MM/yyyy - HH:mm:ss");
		Calendar cal = Calendar.getInstance();
		cal.setTime(df.parse("01/01/2025 - 00:00:00"));
		
		File file= new File(System.getProperty("user.home") + 
				"/nautilus_storage/apache-aurora-0.8.0.tar.gz");
		serverService.keepTheFile(file.getAbsolutePath(), 0, cal, null, file.length(), "file0");
		
		NautilusMessage msg = new NautilusMessage(0, "file0");
		byte[] result = connectionUtilities.processMessageTypeZero(msg);
		
		assertNull(result);
	}
	
	//@Test
	public void procesingMessageTypeZeroCameLateTest() throws Exception {
		/* save the file 0 in the system */
		
		DateFormat df = new SimpleDateFormat("dd/MM/yyyy - HH:mm:ss");
		Calendar cal = Calendar.getInstance();
		cal.setTime(df.parse("01/01/2005 - 00:00:00"));
		
		File file= new File(System.getProperty("user.home") + 
				"/nautilus_storage/apache-aurora-0.8.0.tar.gz");
		serverService.keepTheFile(file.getAbsolutePath(), 0, null, cal, file.length(), "file0");
		
		NautilusMessage msg = new NautilusMessage(0, "file0");
		byte[] result = connectionUtilities.processMessageTypeZero(msg);
		
		assertNull(result);
	}
	
	//@Test
	public void procesingMessageTypeZeroLimitDownloadTest() throws Exception {
		/* save the file 0 in the system */
		File file= new File(System.getProperty("user.home") + 
				"/nautilus_storage/apache-aurora-0.8.0.tar.gz");
		serverService.keepTheFile(file.getAbsolutePath(), 1, null, null, file.length(), "file0");
		
		NautilusMessage msg = new NautilusMessage(0, "file0");
		byte[] result = connectionUtilities.processMessageTypeZero(msg);
		
		assertEquals(result.length, file.length());
		
		byte[] result2 = connectionUtilities.processMessageTypeZero(msg);
		
		assertNull(result2);
	}
	
	
	@Test
	public void packagingMessageTypeOneSuccess() {
		List<NautilusMessage> packs = connectionUtilities.prepareFileToSend("apache-aurora-0.8.0.tar.gz", 
				0, null, null);
		
		System.out.println("hola mundo");
		
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
	
	private static void downloadFileByUrl(String uri, File folder) throws Exception {
		URL url = new URL(uri);
		URLConnection connection = url.openConnection();
		
		InputStream in = connection.getInputStream();
		FileOutputStream out = new FileOutputStream(folder.getAbsoluteFile()+"/"+getNameAboutUri(uri));
		
		byte[] data = new byte[1000];
		int read = in.read(data);
		
		while (read > 0) {
			out.write(data, 0, read);
			read = in.read(data);
		}
		
		in.close();
		out.close();
	}
	
	private static byte[] readContentIntoByteArray(File file) {
	      FileInputStream fileInputStream = null;
	      byte[] bFile = new byte[(int) file.length()];
	      try {
	         //convert file into array of bytes
	         fileInputStream = new FileInputStream(file);
	         fileInputStream.read(bFile);
	         fileInputStream.close();
	      }
	      catch (Exception e) {
	         e.printStackTrace();
	      }
	      return bFile;
	}
	
	private static String getNameAboutUri(String uri) {
		String[] tmp = uri.split("/");
		return tmp[tmp.length - 1];
	}
}
