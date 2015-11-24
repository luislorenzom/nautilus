package es.udc.fic.tic.nautilus.server;

import java.text.ParseException;

import javax.management.InstanceNotFoundException;

import es.udc.fic.tic.nautilus.expcetion.FileUnavaliableException;
import es.udc.fic.tic.nautilus.expcetion.NotSaveException;
import es.udc.fic.tic.nautilus.expcetion.StorageLimitException;
import es.udc.fic.tic.nautilus.model.FileInfo;

public interface ServerService {
	
	/**
	 * This method keep the file in the system and creates a
	 * instance in the database.
	 * 
	 * @param FileInfo f
	 * @param int downloadList
	 * @param String dateLimit
	 * @throws NotSaveException, ParseException
	 * @throws StorageLimitException 
	 */
	public FileInfo keepTheFile(String filePath, int downloadLimit, String releaseDate, 
			String dateLimit, int size, String hash) throws NotSaveException, ParseException, StorageLimitException;
	
	/**
	 * This function search in the database for the file
	 * that containing the hash code
	 * 
	 * @param String hash
	 * @return FileInfo
	 * @throws InstanceNotFoundException 
	 * @throws FileUnavaliableException 
	 */
	public FileInfo returnFile(String hash) throws InstanceNotFoundException, FileUnavaliableException;
}
