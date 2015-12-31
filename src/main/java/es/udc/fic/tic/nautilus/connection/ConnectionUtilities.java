package es.udc.fic.tic.nautilus.connection;

import java.io.File;
import java.util.Calendar;
import java.util.List;

public interface ConnectionUtilities {
	
	/**
	 * This function processed a request which find a file and return this
	 * 
	 * @param NautilusMessage msg
	 * @return The searched file byte array 
	 */
	public byte[] processMessageTypeZero(NautilusMessage msg);
	
	
	/**
	 * This function processed a request which save a file in the system
	 * 
	 * @param NautilusMessage msg
	 * @return integer which represents if the file has been saved (1) or not (-1)
	 */
	public int processMessageTypeOne(NautilusMessage msg);
	
	/**
	 * This function get the parameter of 
	 * 
	 * @param the file hash
	 * @return The message
	 */
	public List<NautilusMessage> getMessagesFromKey(String keyPath);
	
	/**
	 * This function split and encrypt the file, meanwhile save the keys from files
	 * 
	 * @return A list of messages
	 */
	public List<NautilusMessage> prepareFileToSend(String filePath, int downloadLimit,
			Calendar dateLimit, Calendar dateRelease);
	
	/**
	 * This function get the host and the backup from our config
	 * 
	 * @return List with the two host for save the file
	 */
	public List<String> getHostAndBackupFromConfig();
	
	/**
	 * This files receive one list of encrypt files and 
	 * restore the original file
	 * 
	 * @param List<File> files
	 * @param List<NautilusKey> key
	 * @throws Exception 
	 */
	public void restoreFile(List<File> files, List<NautilusKey> key) throws Exception;
}
