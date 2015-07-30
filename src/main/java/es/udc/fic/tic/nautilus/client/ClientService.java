package es.udc.fic.tic.nautilus.client;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.List;

import org.bouncycastle.crypto.DataLengthException;
import org.bouncycastle.crypto.InvalidCipherTextException;

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
	public File encryptFile(String filePath, ENCRYPT_ALG algorithm) 
			throws IOException, DataLengthException, NoSuchAlgorithmException, 
			IllegalStateException, InvalidCipherTextException, Exception; 
	
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
	
	/**
	 * Distributed the file in the p2p network
	 * 
	 * @param files want to distributed
	 */
	public void sendFile(List<File> files);	
	
	/**
	 * recovery the list files to make the final file
	 * 
	 * @param the file with the nodeList where are the files
	 * @return the listFiles
	 */
	public List<File> recoveryFiles(File nodeList);
}
