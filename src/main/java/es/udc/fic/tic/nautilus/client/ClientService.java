package es.udc.fic.tic.nautilus.client;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

import javax.crypto.SecretKey;

import es.udc.fic.tic.nautilus.util.Metadata;
import es.udc.fic.tic.nautilus.util.ModelConstanst.ENCRYPT_ALG;


public interface ClientService {
	
	/**
	 * Split the file in various files
	 * 
	 * @param file want to encrypt
	 * @throws FileNotFoundException 
	 */
	public void fileSplit(String filePath) throws IOException;
	
	/**
	 * Join the files to return the final file
	 * 
	 * @param the files want to join
	 * @throws FileNotFoundException, IOExpcetion
	 */
	public void fileJoin(String baseName) throws FileNotFoundException, IOException;
	
	/**
	 * Encrypt the file with random key generated in this function
	 * 
	 * @param file
	 * @param algorithm, if 1 (RSA) or 0 (AES256)
	 * @return the file encrypted
	 * @throws Exception 
	 */
	public SecretKey encryptFile(String filePath, ENCRYPT_ALG algorithm) throws Exception; 
	
	/**
	 * Decrypt the files
	 * 
	 * @param the file containing the keys
	 * @param the file to decrypt
	 * @return the files decrypted
	 * @throws Exception 
	 */
	public List<File> decrypt(String key, String filePath, ENCRYPT_ALG algorithm) 
			throws Exception;
	
	/**
	 * from metadata object generate XML file
	 * 
	 * @param metadata object with the params
	 * @return XML file with the metadata
	 */
	public File generateMetadata(Metadata metadata);
}
