package es.udc.fic.tic.nautilus.model;

import java.util.List;

import javax.management.InstanceNotFoundException;

/**
 * Interface to FileInfoDao
 * 
 * @author Luis Lorenzo
 */
public interface FileInfoDao extends GenericDao<FileInfo, Long> {
	
	/**
	 * This function return the number of 
	 * files containing in the node
	 * 
	 * @return int NumberOfFiles
	 */
	public int numberOfFiles();
	
	/**
	 * Find the files by name. The reason because 
	 * we return a list instead of only file is
	 * because two files can have the same name
	 * 
	 * @param String name
	 * @return List of files 
	 * @throws InstanceNotFoundException
	 */
	public List<FileInfo> findByName(String name) throws InstanceNotFoundException;
	
	/**
	 * Find the files by the hash code
	 * 
	 * @param String hash
	 * @return File file
	 * @throws InstanceNotFoundException
	 */
	public FileInfo findByHash(String hash) throws InstanceNotFoundException;
	
	/**
	 * Find all the sizes save in the database
	 * 
	 * @return the sum of total sizes
	 */
	public Long getAllSizes();
}
