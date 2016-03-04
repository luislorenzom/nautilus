package es.udc.fic.tic.nautilus.connection;

import java.io.File;
import java.security.PublicKey;
import java.util.Calendar;
import java.util.List;

public interface ConnectionUtilities {
	
	/**
	 * This function processed a request which find a file and return this
	 * 
	 * @param NautilusMessage msg
	 * @return NautilusMessage with the byte[] and boolean that indicate if the file needs sync
	 */
	public NautilusMessage processMessageTypeZero(NautilusMessage msg);
	
	
	/**
	 * This function processed a request which save a file in the system
	 * 
	 * @param NautilusMessage msg
	 * @return integer which represents if the file has been saved (1) or not (-1)
	 */
	public int processMessageTypeOne(NautilusMessage msg);
	
	/**
	 * This function split and encrypt the file, meanwhile save the keys from files
	 * 
	 * @return A list of messages
	 */
	public List<NautilusMessage> prepareFileToSend(String filePath, int downloadLimit,
			Calendar dateLimit, Calendar dateRelease, PublicKey publickey);
	
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
	
	/**
	 * This function decrement the download limit of one file when this one is
	 * downloaded in other server
	 * 
	 * @param NautilusMessage msg
	 * @return int one o zero depends of success of operation
	 */
	public int synchronizeFile(NautilusMessage msg);
	
	/**
	 * This method check and delete all expired files save in the system
	 */
	public void checkAndDeleteExpiredFile();
}
