package es.udc.fic.tic.nautilus.client;

import static es.udc.fic.tic.nautilus.util.ModelConstanst.SPRING_CONFIG_FILE;
import static es.udc.fic.tic.nautilus.util.ModelConstanst.SPRING_CONFIG_TEST_FILE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.security.KeyPair;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import es.udc.fic.tic.nautilus.util.ModelConstanst.ENCRYPT_ALG;
import es.udc.fic.tic.nautilus.util.RSAManager;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { SPRING_CONFIG_FILE, SPRING_CONFIG_TEST_FILE })
@Transactional
public class ClientServiceTest {
	
	@Autowired
	private ClientService clientService;
	
	private RSAManager manager = new RSAManager();
	
	// Download a file to make the test
	@BeforeClass
	public static void downloadFileForTest() throws IOException {
		URL url = new URL("http://www.lavozdegalicia.es/assets/img/logo.png");
		URLConnection connection = url.openConnection();
		
		InputStream in = connection.getInputStream();
		FileOutputStream out = new FileOutputStream("photo.png");
		
		byte[] data = new byte[1000];
		int read = in.read(data);
		
		while (read > 0) {
			out.write(data, 0, read);
			read = in.read(data);
		}
		
		in.close();
		out.close();
	}
	
	// Delete the file we used to make the test
	@AfterClass
	public static void deteleFileForTest() {
		new File("photo.png").delete();
		new File("photo.png.aes256").delete();
		new File("dec_photo.png").delete();
		new File("public.key").delete();
		new File("private.key").delete();
	}
	
	@Test
	public void splitFileTest() throws IOException {
		File file = new File("photo.png");
		File dir = new File(".");
		// count the files before split
		int numberOfFilesBefore = this.CountDirFiles(dir);
		clientService.fileSplit(file.getAbsolutePath());
		assertNotNull(file);
		
		// count the files after split
		int numberOfFilesAfter = this.CountDirFiles(dir);
		
		// calculate the difference
		int difference = (int) Math.ceil((float) (file.length() / generateChunkSize(file.length()) + 1 ));
		
		assertEquals(numberOfFilesAfter, numberOfFilesBefore+difference);
		
		this.DeleteSplitFiles(dir, file.getName());
	}
	
	@Test
	public void fileJoinTest() throws IOException {
		File dir = new File(".");
		int originalLenght = (int) new File("photo.png").length();
		clientService.fileSplit("photo.png");
		new File("photo.png").delete();
		clientService.fileJoin("photo.png");
		
		File file = new File("photo.png");
		assertNotNull(file);
		assertEquals(file.length(), originalLenght);
		
		this.DeleteSplitFiles(dir, "photo.png");
	}
	
	@Test
	public void encryptAndDecryptWithAesTest() throws Exception {
		KeyContainer key = clientService.encryptFile("photo.png", ENCRYPT_ALG.AES, null);
		File file = new File("photo.png.aes256");
		assertNotNull(file);
		assertNotNull(new File(generateKeyName("photo.png")));
		
		clientService.decrypt(key.getKey(), "photo.png.aes256", ENCRYPT_ALG.AES);
		File decodeFile = new File("dec_photo.png");
		assertNotNull(decodeFile);
 		assertEquals(new File("photo.png").length(), decodeFile.length());
	}
	
	@Test
	public void encryptAndDecryptWithRSATest() throws Exception {
		KeyPair pair = manager.generateKeys();
		KeyContainer key = clientService.encryptFile("photo.png", ENCRYPT_ALG.RSA, pair.getPublic());
		File file = new File("photo.png.aes256");
		assertNotNull(file);
		assertNotNull(new File(generateKeyName("photo.png")));
		
		clientService.decrypt(key.getKey(), "photo.png.aes256", ENCRYPT_ALG.RSA);
		File decodeFile = new File("dec_photo.png");
		assertNotNull(decodeFile);
 		assertEquals(new File("photo.png").length(), decodeFile.length());
	}
	
	/*********************
	 * Private functions *
	 *********************/
	
	/**
	 * function to count the number of files that there in a directory
	 * 
	 * @param dir
	 * @return the number of files
	 */
	private int CountDirFiles(File dir) {
		int filesInDir = 0;
		
		if (dir.exists()) {
			File[] files = dir.listFiles();
			for (int x = 0; x < files.length; x++) {
				filesInDir++;
			}
		}
		return filesInDir;
	}
	
	/**
	 * This method delete all split files in the classpath
	 * 
	 * @param dir
	 */
	private void DeleteSplitFiles(File dir, String nameFile) {
		if (dir.exists()) {
			File[] files = dir.listFiles();
			for (int x = 0; x < files.length; x++) {
				
				if (!(files[x].getName().equals(nameFile))) {
					if (files[x].getName().contains(nameFile)) {
						files[x].delete();
					}
				}
			}
		}
	}
	
	private long generateChunkSize(long fileSize) {
		
		/* less than 50mb implies split in three parts */
		if (fileSize <= 52428800) {
			return (fileSize / 2) - 100;
		}
		
		/* less than 250mb implies split in five parts */
		if (fileSize <= 262144000) {
			return (fileSize / 4) - 100;
		}
		
		/* less than 1gb implies split in nine parts */
		if (fileSize <= 1073741824) {
			return (fileSize / 8) - 100;
		}
		
		/* Default */
		return (fileSize / 16) - 100;
	}
	
	private String generateKeyName(String fileName) {
		
		String finalName = "";
		String[] chunks = fileName.split(".");
		for (String chunk : chunks) {
			finalName += chunk;
		}
		return finalName + "AESKey.txt";
	}
}
