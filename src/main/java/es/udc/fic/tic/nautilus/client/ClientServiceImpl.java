package es.udc.fic.tic.nautilus.client;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.security.NoSuchAlgorithmException;
import java.util.List;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

import org.bouncycastle.crypto.DataLengthException;
import org.bouncycastle.crypto.InvalidCipherTextException;
import org.springframework.stereotype.Service;

import es.udc.fic.tic.nautilus.util.Metadata;
import es.udc.fic.tic.nautilus.util.ModelConstanst.ENCRYPT_ALG;



@Service("clientService")
public class ClientServiceImpl implements ClientService {
	
	// FIXME el chunkSize debe depender del fichero
	private long chunkSize = 1024;	//1KB

	public void fileSplit(String filePath) throws IOException {
		// Open the file
		BufferedInputStream in = new BufferedInputStream(new FileInputStream(filePath));
		
		// Get the file length
		File file = new File(filePath);
		long fileSize = file.length();
		
		// Loop for each full chunk
		int subfile;
		for (subfile = 0; subfile < fileSize/chunkSize; subfile++) {
			// Open the output file
			BufferedOutputStream out = new BufferedOutputStream(new 
					FileOutputStream(filePath + "." + subfile));
			
			// Write the right amount of bytes
			for (int currentByte = 0; currentByte < chunkSize; currentByte++) {
				// Load one byte from the input file and write it to the output file
				out.write(in.read());
			}
			// Close the file
			out.close();
		}
		
		// Loop for the last chunk (which may be smaller than the chunk size)
		if (fileSize != chunkSize * (subfile - 1)) {
			// Open the output file
			BufferedOutputStream out = new BufferedOutputStream(
					new FileOutputStream(filePath + "." + subfile));
			
			// Write the rest of the file
			int b;
			while ((b = in.read()) != -1) {
				out.write(b);
			}
			// Close the file
			out.close();
		}
		// Close the file
		in.close();
	}
	
	public void fileJoin(String baseName) throws FileNotFoundException, IOException {
		int parts = this.getNumberParts(baseName);
		
		// Now, assume that the files are correctly numbered in order
		BufferedOutputStream out = new BufferedOutputStream(new 
				FileOutputStream(baseName));
		for (int part = 0; part < parts; part++) {
			BufferedInputStream in = new BufferedInputStream(new 
					FileInputStream(baseName + "." + part));
			int b;
			while ( (b = in.read()) != -1 ) {
				out.write(b);
			}
			in.close();
		}
		out.close();
	}

	public File encryptFile(String filePath, ENCRYPT_ALG algorithm) 
			throws Exception {
		
		switch (algorithm) {
		case RSA:
			/* llamar a una funcion privada que cifre el fichero con rsa */
			break;

		case AES:
			// Encrypt
			this.encryptWithAES(filePath);
			break;
		}
		return null;
	}

	public List<File> decrypt(String keyPath, String filePath, ENCRYPT_ALG algorithm) 
			throws Exception {
		
		switch (algorithm) {
		case RSA:
			/* llamar a una funcion privada que descifre el fichero con rsa */
			break;

		case AES:
			// Decrypt
			this.decryptWithAES(filePath);
			break;
		}
		return null;
	}
	
	public File generateMetadata(Metadata metadata) {
		// TODO Auto-generated method stub
		return null;
	}
	
	public void sendFile(List<File> files) {
		// TODO Auto-generated method stub
		
	}

	public List<File> recoveryFiles(File nodeList) {
		// TODO Auto-generated method stub
		return null;
	}
	
	
	/*********************
	 * Private functions *
	 *********************/
	
	/**
	 * Return the number of files split
	 * 
	 * @param baseName
	 * @return the number of parts
	 */
	private int getNumberParts(String baseName) {
		// List all files in the same directory
		File directory = new File(baseName).getAbsoluteFile().getParentFile();
		final String justFileName = new File(baseName).getName();
		String[] matchingFiles = directory.list(new FilenameFilter() {
			
			public boolean accept(File dir, String name) {
				return name.startsWith(justFileName) && 
						name.substring(justFileName.length()).matches("^\\.\\d+$");
			}
			
		});
		return matchingFiles.length;
	}
	
	/**
	 * This function Encrypt the file and generate the keyFile
	 * 
	 * @param fileContent
	 * @param fileName
	 * 
	 * @throws NoSuchAlgorithmException
	 * @throws DataLengthException
	 * @throws IllegalStateException
	 * @throws InvalidCipherTextException
	 * @throws IOException
	 */
	private void encryptWithAES(String fileName) throws Exception {
		// Generate Key
		KeyGenerator keyGen = KeyGenerator.getInstance("AES");
		keyGen.init(256);
		SecretKey key = keyGen.generateKey();
		// Generate cipher for AES
		Cipher aesCipher = Cipher.getInstance("AES");
		aesCipher.init(Cipher.ENCRYPT_MODE, key);
		
		try(FileOutputStream fos = new FileOutputStream(fileName+".aes")) {
			//creating object output stream to write objects to file
			ObjectOutputStream oos = new ObjectOutputStream(fos);
			oos.writeObject(key);  //saving key to file for use during decryption
			 
			 //creating file input stream to read contents for encryption
			try(FileInputStream fis = new FileInputStream(fileName)){
				//creating cipher output stream to write encrypted contents
			    try(CipherOutputStream cos = new CipherOutputStream(fos, aesCipher)){
			    	int read;
			    	byte buf[] = new byte[4096];
			    	while((read = fis.read(buf)) != -1)  //reading from file
			    		cos.write(buf, 0, read);  //encrypting and writing to file
			    }
			}
		}
	}
	
	private void decryptWithAES(String fileName) throws Exception {
		SecretKey key = null;
		
	  //creating file input stream to read from file
		try(FileInputStream fis = new FileInputStream(fileName)) {
		   //creating object input stream to read objects from file
		   ObjectInputStream ois = new ObjectInputStream(fis);
		   key = (SecretKey)ois.readObject();  //reading key used for encryption
		   
		   Cipher aesCipher = Cipher.getInstance("AES");  //getting cipher for AES
		   aesCipher.init(Cipher.DECRYPT_MODE, key);  //initializing cipher for decryption 
		   //with key creating file output stream to write back original contents
		   
		   try(FileOutputStream fos = new FileOutputStream("dec_"+fileName.substring(0
				   , fileName.length()-4))) {
			   //creating cipher input stream to read encrypted contents
			   try(CipherInputStream cis = new CipherInputStream(fis, aesCipher)) {
				   int read;
				   byte buf[] = new byte[4096];
				   while((read = cis.read(buf)) != -1)  //reading from file
					   fos.write(buf, 0, read);  //decrypting and writing to file
			   }
		   }
		}
	  }
	
	
	/* tendra que recibir las claves publicas como parametros?? */
	@SuppressWarnings("unused")
	private void encryptWithRSA(String filePath) {
		// TODO
	}
}