package es.udc.fic.tic.nautilus.server;

import java.text.ParseException;

import javax.management.InstanceNotFoundException;

import org.hyperic.sigar.SigarException;

import es.udc.fic.tic.nautilus.expcetion.FileUnavaliableException;
import es.udc.fic.tic.nautilus.expcetion.NotSaveException;
import es.udc.fic.tic.nautilus.model.FileInfo;
import es.udc.fic.tic.nautilus.util.SystemStatistics;

public interface ServerService {
	/**
	 * This function obtain and analyze the specs of the
	 * system and determinate if the computer is candidate
	 * to make a server. 
	 * 
	 * @return Object "SystemStatics" with the required field
	 * for the heuristic
	 * @throws SigarException 
	 */
	public SystemStatistics obtainStatics() throws SigarException;
	
	/**
	 * This method keep the file in the system and creates a
	 * instance in the database.
	 * 
	 * @param FileInfo f
	 * @param int downloadList
	 * @param String dateLimit
	 * @throws NotSaveException, ParseException
	 */
	public FileInfo keepTheFile(String filePath, int downloadLimit, String releaseDate, 
			String dateLimit, double size, String hash) throws NotSaveException, ParseException;
	
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
