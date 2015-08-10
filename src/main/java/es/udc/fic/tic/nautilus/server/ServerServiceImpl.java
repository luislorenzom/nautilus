package es.udc.fic.tic.nautilus.server;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.management.InstanceNotFoundException;

import org.hyperic.sigar.FileSystem;
import org.hyperic.sigar.FileSystemUsage;
import org.hyperic.sigar.Sigar;
import org.hyperic.sigar.SigarException;
import org.hyperic.sigar.SigarProxy;
import org.hyperic.sigar.SigarProxyCache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import es.udc.fic.tic.nautilus.expcetion.FileUnavaliableException;
import es.udc.fic.tic.nautilus.expcetion.NotSaveException;
import es.udc.fic.tic.nautilus.model.FileInfo;
import es.udc.fic.tic.nautilus.model.FileInfoDao;
import es.udc.fic.tic.nautilus.util.FileSystemInfo;
import es.udc.fic.tic.nautilus.util.SystemStatistics;


@Service("serverService")
public class ServerServiceImpl implements ServerService {
	
	@Autowired
	private FileInfoDao fileInfoDao;
	
	public SystemStatistics obtainStatics() throws SigarException {
		this.preloadSigar();
		Sigar sigar = new Sigar();
		SystemStatistics stats = new SystemStatistics();
		
		stats.setPingTime(this.makePing());
		stats.setUptime(this.calculateUptime(sigar));
		stats.setFileSystems(this.calculateTotalSpace(sigar));
		
		return stats;
	}

	public FileInfo keepTheFile(String path, int downloadLimit, String releaseDate, 
			String dateLimit, double size, String hash) throws NotSaveException, ParseException {
		
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
	 * This method return the uptime computer in human version
	 * 
	 * @param sigar
	 * @return String with the time in human format
	 * @throws SigarException
	 */
	@SuppressWarnings("unused")
	private String calculateUptimeHuman(Sigar sigar) throws SigarException {
		double uptime = sigar.getUptime().getUptime();
		String humanUptime = "";
		int days, mins, hours;
		
		days = (int) uptime / (60*60*24);
		if (days != 0) {
			humanUptime += days + " " + ((days > 1) ? "días" : "día") + ", ";
		}
		mins = (int) uptime/60;
		hours = (int) mins/60;
		hours %= 24;
		mins %= 60;
		if (hours != 0) {
			humanUptime += hours + ":" + (mins < 10 ? "0" + mins : mins);
		} else {
			humanUptime += mins + "min";
		}
		return humanUptime;
	}
	
	/**
	 * This function return the uptime in double format
	 * 
	 * @param sigar
	 * @return the double with the uptime
	 * @throws SigarException
	 */
	private double calculateUptime(Sigar sigar) throws SigarException {
		return sigar.getUptime().getUptime();
	}
	
	/**
	 * This function return the info of all file system in the computer
	 * 
	 * @param sigar
	 * @return list with all info of file system
	 * @throws SigarException
	 */
	private List<FileSystemInfo> calculateTotalSpace(Sigar sigar) throws SigarException {
		List<FileSystemInfo> listFileSystem = new ArrayList<FileSystemInfo>();
		
		SigarProxy proxy = SigarProxyCache.newInstance(sigar);
		FileSystem[] fileSystemList = proxy.getFileSystemList(); 
		for (int i = 0; i < fileSystemList.length; i++) {
			FileSystemInfo fileSystemInfo = getInfoFileSystem(fileSystemList[i], sigar);
			listFileSystem.add(fileSystemInfo);
		}
		
		return listFileSystem;
	}
	
	/**
	 * This function obtain the info of the fileSystem requested
	 * 
	 * @param fileSystem
	 * @param sigar
	 * @return the info of the fileSystem requested
	 */
	private FileSystemInfo getInfoFileSystem(FileSystem fileSystem, Sigar sigar) {
		long used, avaliable, total, percentage;
		try {
			FileSystemUsage use;
			use = sigar.getFileSystemUsage(fileSystem.getDirName());
			
			used = use.getTotal() - use.getFree();
			avaliable = use.getAvail();
			total = use.getTotal();
			percentage = (long) (use.getUsePercent() * 100);
		} catch (SigarException e) {
			used = avaliable = total = percentage = 0;
		}
		
		return new FileSystemInfo(used, avaliable, total, percentage, 
				fileSystem.getDirName(), fileSystem.getSysTypeName());
	}
	
	/**
	 * Make ping google to check the latency
	 * 
	 * @return the latency in milliseconds, if can't connect
	 * return values -1
	 */
	private long makePing() {
		HttpURLConnection connection = null;
		long ping = -1;
		try{
            long inicio = System.currentTimeMillis();
            URL u = new URL("http://www.google.com");
            connection = (HttpURLConnection) u.openConnection();
            connection.setRequestMethod("HEAD");

            long fin = System.currentTimeMillis();
            ping = fin - inicio;
            
		} catch(MalformedURLException e) {
			System.out.println("Error URL: " +e);
		} catch (IOException e) {
			System.out.println("Error connection: " +e);
		} finally {
			if (connection != null) {
				connection.disconnect();
			}
		}
		return ping;
	}
	
	
	//private static void preloadSigar() {
	private void preloadSigar() {
        //String arch = System.getProperty("os.arch");
        String libName;
/*
        if (SystemUtils.IS_OS_WINDOWS) {
            if (arch.equalsIgnoreCase("x86")) 
                libName = "sigar-x86-winnt";
            else
                libName = "sigar-amd64-winnt";
        } else if (SystemUtils.IS_OS_MAC_OSX) {
            if (arch.startsWith("i") && arch.endsWith("86"))
                libName = "sigar-universal-macosx";
            else
                libName = "sigar-universal64-macosx";
        } else {
            throw new RuntimeException("Unrecognized platform!");

        }*/
        libName = "sigar-universal64-macosx";
        System.setProperty("org.hyperic.sigar.path", "-");    
        System.loadLibrary(libName);
     }
}