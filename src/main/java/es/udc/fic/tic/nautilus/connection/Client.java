package es.udc.fic.tic.nautilus.connection;

import java.util.Calendar;

public interface Client {
	
	/**
	 * This function send the file into and generate the key.xml
	 * 
	 * @param String filePath
	 * @param int downloadLimit
	 * @param Calendar dataLimit
	 * @param Calendar dataRelease
	 */
	public void saveFileInNetwork(String filePath, int downloadLimit, Calendar dateLimit, Calendar dateRelease) throws Exception;

	/**
	 * this function recovery the file from the key file
	 * 
	 * @param String keyPath
	 */
	public void getFileFromKey(String keyPath);
}
