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

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import es.udc.fic.tic.nautilus.util.ModelConstanst.ENCRYPT_ALG;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { SPRING_CONFIG_FILE, SPRING_CONFIG_TEST_FILE })
@Transactional
public class ClientServiceTest {
	
	@Autowired
	private ClientService clientService;
	
	// Download a file to make the test
	@Before
	public void downloadFileForTest() throws IOException {
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
	@After
	public void deteleFileForTest() {
		new File("photo.png").delete();
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
		int difference = (int) Math.ceil((float) (file.length() / 1024f));
		
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
	public void encryAndDecrypttWithAesTest() throws Exception {
		
		clientService.encryptFile("photo.png", ENCRYPT_ALG.AES);
		File file = new File("photo.png.aes");
		assertNotNull(file);
		
		clientService.decrypt("key.txt", "photo.png.aes", ENCRYPT_ALG.AES);
		
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
	

}
