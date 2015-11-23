package es.udc.fic.tic.nautilus.connection;

import java.util.Calendar;

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
	 * This function makes a type zero message
	 * 
	 * @param the file hash
	 * @return The message
	 */
	public NautilusMessage packagingMessageTypeZero(String hash);
	
	/**
	 * This function makes a type one message
	 * 
	 * @return The message
	 */
	public NautilusMessage packagingMessageTypeOne(String filePath, int downloadLimit,
			Calendar dateLimit, Calendar releaseDate);
}
