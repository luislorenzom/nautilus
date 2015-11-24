package es.udc.fic.tic.nautilus.model;

import java.util.Calendar;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

/**
 * File Info entity
 * 
 * @author Luis Lorenzo
 */
@Entity
public class FileInfo {
	
	@Id
	@GeneratedValue
	private Long id;
	private String hash;
	private String path;
	private int downloadLimit;
	private Calendar releaseDate;
	private Calendar dateLimit;
	private int size;
	
	public FileInfo() {}
	
	public FileInfo(String hash, String path, int downloadLimit, Calendar releaseDate, 
			Calendar dateLimit, int size) {
		this.setHash(hash);
		this.setPath(path);
		this.setDownloadLimit(downloadLimit);
		this.setReleaseDate(releaseDate);
		this.setDateLimit(dateLimit);
		this.setSize(size);
	}
	
	
	public Long getId() {
		return id;
	}
	
	public void setId(Long id) {
		this.id = id;
	}
	
	public String getHash() {
		return hash;
	}

	public void setHash(String hash) {
		this.hash = hash;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}
	
	public int getDownloadLimit() {
		return downloadLimit;
	}

	public void setDownloadLimit(int downloadLimit) {
		this.downloadLimit = downloadLimit;
	}
	
	public Calendar getDateLimit() {
		return dateLimit;
	}

	public void setDateLimit(Calendar dateLimit) {
		this.dateLimit = dateLimit;
	}
	
	public Calendar getReleaseDate() {
		return releaseDate;
	}

	public void setReleaseDate(Calendar releaseDate) {
		this.releaseDate = releaseDate;
	}
	
	public int getSize() {
		return size;
	}

	public void setSize(int size) {
		this.size = size;
	}
	
	@Override
	public String toString() {
		return "File [id="+id+", hash="+hash+", path="+path+", "
				+ "downloadLimit="+downloadLimit+", releaseDate="+releaseDate+"dateLimit="+dateLimit+
				", size="+size+"]";
	}
}
