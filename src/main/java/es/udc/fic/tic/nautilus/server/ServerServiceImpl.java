package es.udc.fic.tic.nautilus.server;

import java.io.File;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.management.InstanceNotFoundException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import es.udc.fic.tic.nautilus.config.ConfigHandler;
import es.udc.fic.tic.nautilus.expcetion.FileUnavaliableException;
import es.udc.fic.tic.nautilus.expcetion.NotHaveDownloadLimitException;
import es.udc.fic.tic.nautilus.expcetion.NotSaveException;
import es.udc.fic.tic.nautilus.model.FileInfo;
import es.udc.fic.tic.nautilus.model.FileInfoDao;


@Service("serverService")
public class ServerServiceImpl implements ServerService {
	
	@Autowired
	private FileInfoDao fileInfoDao;
	
	@Transactional
	public FileInfo keepTheFile(String path, int downloadLimit, Calendar releaseDate, 
			Calendar dateLimit, long size, String hash) throws NotSaveException, ParseException {
		
		FileInfo file = new FileInfo();
		file.setPath(path);
		file.setSize(size);
		
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
			/*DateFormat df = new SimpleDateFormat("dd/MM/yyyy - HH:mm:ss");
			Calendar cal = Calendar.getInstance();
			cal.setTime(df.parse(releaseDate));*/
			file.setReleaseDate(releaseDate);
		}
		
		/* Date limit download */
		if (dateLimit == null) {
			file.setDateLimit(null);
		} else {
			/*DateFormat df = new SimpleDateFormat("dd/MM/yyyy - HH:mm:ss");
			Calendar cal = Calendar.getInstance();
			cal.setTime(df.parse(dateLimit));*/
			file.setDateLimit(dateLimit);
		}
		
		/* Add hash */
		file.setHash(hash);
		
		fileInfoDao.save(file);
		
		return file;
	}
	
	@Transactional
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
				fileInfoDao.delete(file);
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
			file.setDownloadLimit(downloadLimit-1);
			//if (file.getDownloadLimit() == 0) {
			if (downloadLimit == 0) {
				/* if limit is zero then is deleted */
				fileInfoDao.delete(file);
				File realFile = new File(file.getPath());
				realFile.delete();
				return null;
			}
			return file;
		}
	}
	
	@Transactional
	public boolean checkFileSize(Long fileSize) {
		ConfigHandler configHandler = new ConfigHandler();
		long limit = configHandler.getConfig().getLimitSpace();
		
		/* all disk */
		if (limit <= -1) {
			return true;
		}
		
		long folderSize = fileInfoDao.getAllSizes();
		
		if (folderSize + fileSize > limit) {
			return false;
		}
		return true;
	}
	
	@Transactional
	public void decrementDownloadLimit(String hash) throws NotHaveDownloadLimitException {
		try {
			FileInfo file = fileInfoDao.findByHash(hash);
			if (file.getDownloadLimit() > -1) {
				/* Have download limit */
				int limit = file.getDownloadLimit();
				file.setDownloadLimit(limit - 1);
			} else {
				throw new NotHaveDownloadLimitException();
			}
		} catch (Exception e) {
			throw new NotHaveDownloadLimitException();
		}
	}

	@Transactional
	public void deleteAllExpiratedFiles() {
		List<FileInfo> expiratedFiles = fileInfoDao.findAllExpiratedFiles();
		
		for (FileInfo file : expiratedFiles) {
			try {
				new File(file.getPath()).delete();
				fileInfoDao.delete(file);
			} catch (Exception e) {
				System.err.println("Error trying to delete a entity");
			}
		}
	}
	
	@Transactional
	public void deleteFile(String hash) {
		try {
			FileInfo fileInfo = fileInfoDao.findByHash(hash);
			new File(fileInfo.getPath()).delete();
			fileInfoDao.delete(fileInfo);
		} catch (Exception e) {
			System.err.println("Can't delete the file");
		}
	}
}