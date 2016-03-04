package es.udc.fic.tic.nautilus.server;

import java.text.ParseException;
import java.util.Calendar;

import javax.management.InstanceNotFoundException;

import es.udc.fic.tic.nautilus.expcetion.FileUnavaliableException;
import es.udc.fic.tic.nautilus.expcetion.NotHaveDownloadLimitException;
import es.udc.fic.tic.nautilus.expcetion.NotSaveException;
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
	public FileInfo keepTheFile(String filePath, int downloadLimit, Calendar releaseDate, 
			Calendar dateLimit, long size, String hash) throws NotSaveException, ParseException;
	
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
	
	/**
	 * Check if one file can or not save in the system
	 * 
	 * @param Long fileSize
	 * @return boolean for save or not the file
	 */
	public boolean checkFileSize(Long fileSize);
	
	/**
	 * This function decrement the download limit of one file
	 * 
	 * @param String hash
	 * @throws throws NotHaveDownloadLimitException
	 */
	public void decrementDownloadLimit(String hash) throws NotHaveDownloadLimitException;
	
	/**
	 * This method delete all expirated file save in our system
	 */
	public void deleteAllExpiratedFiles();
	
	/**
	 * This function is destined to delete files when they exceed the 
	 * download limit  or when they are corrupted
	 * 
	 * @param String hash
	 */
	public void deleteFile(String hash);
}
