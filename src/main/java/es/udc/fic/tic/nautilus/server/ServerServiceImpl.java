package es.udc.fic.tic.nautilus.server;

import java.io.File;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import javax.management.InstanceNotFoundException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import es.udc.fic.tic.nautilus.config.ConfigHandler;
import es.udc.fic.tic.nautilus.expcetion.FileUnavaliableException;
import es.udc.fic.tic.nautilus.expcetion.NotSaveException;
import es.udc.fic.tic.nautilus.expcetion.StorageLimitException;
import es.udc.fic.tic.nautilus.model.FileInfo;
import es.udc.fic.tic.nautilus.model.FileInfoDao;


@Service("serverService")
public class ServerServiceImpl implements ServerService {
	
	@Autowired
	private FileInfoDao fileInfoDao;

	public FileInfo keepTheFile(String path, int downloadLimit, String releaseDate, 
			String dateLimit, int size, String hash) throws NotSaveException, ParseException, StorageLimitException {
		
		FileInfo file = new FileInfo();
		file.setPath(path);
		file.setSize(size);
		File storageFolder = new File(new ConfigHandler().getConfig().getStorageFolder());
		int limitSpace = new ConfigHandler().getConfig().getLimitSpace();
		
		/* Check the space limit in the config file */
		if ((limitSpace != 0) && (folderSize(storageFolder) + size > limitSpace)) {
			throw new StorageLimitException();
		}
		
		/* Download limit */
		if (downloadLimit < 1) {
			file.setDownloadLimit(-1);
		} else {
			file.setDownloadLimit(downloadLimit);
		}
		
		/* Release date */
		if (releaseDate == null) {
			file.setReleaseDate(null);
		} else {
			DateFormat df = new SimpleDateFormat("dd/MM/yyyy - HH:mm:ss");
			Calendar cal = Calendar.getInstance();
			cal.setTime(df.parse(releaseDate));
			file.setReleaseDate(cal);
		}
		
		/* Date limit download */
		if (dateLimit == null) {
			file.setDateLimit(null);
		} else {
			DateFormat df = new SimpleDateFormat("dd/MM/yyyy - HH:mm:ss");
			Calendar cal = Calendar.getInstance();
			cal.setTime(df.parse(dateLimit));
			file.setDateLimit(cal);
		}
		
		/* Add hash */
		file.setHash(hash);
		
		fileInfoDao.save(file);
		
		return file;
	}
	
	public FileInfo returnFile(String hash) throws InstanceNotFoundException, FileUnavaliableException {
		FileInfo file = fileInfoDao.findByHash(hash);
		
		
		/* Check the Release Date */
		if (file.getReleaseDate() != null) {
			Date date = Calendar.getInstance().getTime();
			Calendar now = Calendar.getInstance();
			now.setTime(date);
			
			if (now.before(file.getReleaseDate())) {
				throw new FileUnavaliableException();
			}
		}
		
		/* Check the time limits */
		if (file.getDateLimit() != null) {
			Date date = Calendar.getInstance().getTime();
			Calendar now = Calendar.getInstance();
			now.setTime(date);
			
			/* Check the dates */
			if (now.after(file.getDateLimit())) {
				File realFile = new File(file.getPath());
				realFile.delete();
				return null;
			}
		}
		
		/* Now we check the limits */
		if (file.getDownloadLimit() == -1) {
			/* the file not have limit */
			return file;
		} else {
			/* decrement the limit */
			int downloadLimit = file.getDownloadLimit();
			if (downloadLimit == 0) {
				/* if limit is zero then is deleted */
				fileInfoDao.delete(file);
				File realFile = new File(file.getPath());
				realFile.delete();
				return null;
			}
			file.setDownloadLimit(downloadLimit-1);
			return file;
		}
	}
	
	
	/*********************
	 * Private functions *
	 *********************/
	/**
	 * This function calculate the size of directory
	 */
	private long folderSize(File directory) {
	    long length = 0;
	    for (File file : directory.listFiles()) {
	        if (file.isFile())
	            length += file.length();
	        else
	            length += folderSize(file);
	    }
	    return length;
	}
}