package es.udc.fic.tic.nautilus.server;

import static es.udc.fic.tic.nautilus.util.ModelConstanst.SPRING_CONFIG_FILE;
import static es.udc.fic.tic.nautilus.util.ModelConstanst.SPRING_CONFIG_TEST_FILE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import javax.management.InstanceNotFoundException;

import org.hyperic.sigar.SigarException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import es.udc.fic.tic.nautilus.expcetion.NotSaveException;
import es.udc.fic.tic.nautilus.model.FileInfo;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { SPRING_CONFIG_FILE, SPRING_CONFIG_TEST_FILE })
@Transactional
public class ServerServiceTest {
	
	@Autowired
	private ServerService serverService;
	
	@Test
	public void keepFileAllFieldsTest() throws NotSaveException, ParseException {
		FileInfo file = serverService.keepTheFile("~/nautilus/download1.aes", 3, 
				"17/12/2017 - 00:00:00", 
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
	public void keepFileWithDownloadLimitNegativeTest() throws NotSaveException, ParseException {
		FileInfo file = serverService.keepTheFile("~/nautilus/download1.aes", -3, 
				"17/12/2017 - 00:00:00", 
				123.33, "21a57f2fe765e1ae4a8bf15d73fc1bf2a533f547f2343d12a499d9c0592044d4");
		
		assertEquals(file.getDownloadLimit(), -1);
	}
	
	@Test
	public void keepFileWithoutDateLimitTest() throws NotSaveException, ParseException {
		FileInfo file = serverService.keepTheFile("~/nautilus/download1.aes", 3, 
				null, 123.33, "21a57f2fe765e1ae4a8bf15d73fc1bf2a533f547f2343d12a499d9c0592044d4");
		
		assertNull(file.getDateLimit());
	}
	
	@Test
	public void getFileByHashWithoutDateLimitAndDownloadLimitTest() 
			throws NotSaveException, ParseException, InstanceNotFoundException {
		
		FileInfo file = serverService.keepTheFile("~/nautilus/download1.aes", -1, 
				null, 123.33, "21a57f2fe765e1ae4a8bf15d73fc1bf2a533f547f2343d12a499d9c0592044d4");
		FileInfo fileRecovered = serverService.returnFile("21a57f2fe765e1ae4a8bf15"
				+ "d73fc1bf2a533f547f2343d12a499d9c0592044d4");
		
		assertEquals(file, fileRecovered);
	}
	
	@Test(expected=IndexOutOfBoundsException.class)
	public void getFileByHashInstanceNotFoundTest()
			throws NotSaveException, ParseException, InstanceNotFoundException {
	
		serverService.returnFile("21a57f2fe765e1ae4a8bf15d73fc1bf2a533f547f2343"
				+ "d12a499d9c0592044d4");
	}
	
	@Test
	public void decrementDownloadLimitTest() throws NotSaveException, ParseException, 
	InstanceNotFoundException {
		
		String hash = "21a57f2fe765e1ae4a8bf15d73fc1bf2a533f547f2343d12a499d9c0592044d4";
		FileInfo file = serverService.keepTheFile("~/nautilus/download1.aes", 2, 
				null, 123.33, hash);
		
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
	InstanceNotFoundException {
		
		String hash = "21a57f2fe765e1ae4a8bf15d73fc1bf2a533f547f2343d12a499d9c0592044d4";
		FileInfo file = serverService.keepTheFile("~/nautilus/download1.aes", 0, 
				null, 123.33, hash);
		
		serverService.returnFile(hash);
		assertEquals(file.getDownloadLimit(), -1);
		FileInfo finalFile = serverService.returnFile(hash);
		assertEquals(file.getDownloadLimit(), -1);
		assertEquals(file, finalFile);
	}
	
	@Test
	public void underDateLimitTest() throws NotSaveException, ParseException, 
	InstanceNotFoundException {
		
		String hash = "21a57f2fe765e1ae4a8bf15d73fc1bf2a533f547f2343d12a499d9c0592044d4";
		serverService.keepTheFile("~/nautilus/download1.aes", 0, 
				"12/02/2015 - 00:00:00", 123.33, hash);
		
		FileInfo  file = serverService.returnFile(hash);
		assertNull(file);
	}
	
	@Test
	public void highDateLimitTest() throws NotSaveException, ParseException, 
	InstanceNotFoundException {
		
		String hash = "21a57f2fe765e1ae4a8bf15d73fc1bf2a533f547f2343d12a499d9c0592044d4";
		FileInfo initialFile = serverService.keepTheFile("~/nautilus/download1.aes", 0, 
				"12/12/2018 - 00:00:00", 123.33, hash);
		
		FileInfo file = serverService.returnFile(hash);
		assertEquals(initialFile, file);
	}
	
	//@Test
	public void checkSystemVariables() throws SigarException {
		System.out.println("**************************************");
		// FIXME Problema con la libreria silgar
		System.out.println(serverService.obtainStatics().toString());
		System.out.println("**************************************");
	}

}
