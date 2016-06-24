package es.udc.fic.tic.nautilus.connection;

import java.io.Serializable;
import java.util.Calendar;

public class NautilusMessage implements Serializable{
	
	private static final long serialVersionUID = 1L;
	private int type;
	private String hash;
	private byte[] content;
	private int downloadLimit;
	private Calendar dateLimit;
	private String dateLimitString;
	private Calendar releaseDate;
	private String releaseDateString;
	private boolean synchronize;
	
	/* This constructor will be use to create file request
	 * or to make synchronize petitions to other servers */
	public NautilusMessage(int type, String hash) {
		super();
		this.type = type;
		this.hash = hash;
	}
	
	
	/* This constructor will be use to create a request to save file */
	public NautilusMessage(int type, String hash, byte[] content, int downloadLimit,
			Calendar dataLimit, Calendar releaseDate) {
		super();
		this.type = type;
		this.hash = hash;
		this.content = content;
		this.downloadLimit = downloadLimit;
		this.dateLimit = dataLimit;
		this.releaseDate = releaseDate;
	}
	
	/* This constructor will be use to create a request to save file from Android devices */
	public NautilusMessage(String dataLimitString, String releaseDateString, int type, String 
			hash, byte[] content, int downloadLimit) {
		super();
		this.type = type;
		this.hash = hash;
		this.content = content;
		this.downloadLimit = downloadLimit;
		this.dateLimitString = dataLimitString;
		this.releaseDateString = releaseDateString;
	}
	

	/* This constructor will be use to return the files from server to client */
	public NautilusMessage(byte[] content, boolean synchronize) {
		super();
		this.content = content;
		this.synchronize = synchronize;
	}
	
	
	public int getType() {
		return type;
	}
	
	public void setType(int type) {
		this.type = type;
	}
	
	
	public String getHash() {
		return hash;
	}
	
	public void setHash(String hash) {
		this.hash = hash;
	}
	
	
	public byte[] getContent() {
		return content;
	}
	
	public void setContent(byte[] content) {
		this.content = content;
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
	
	
	public boolean getSynchronize() {
		return synchronize;
	}
	
	public void setSynchronize(boolean synchronize) {
		this.synchronize = synchronize;
	}


	public String getDateLimitString() {
		return dateLimitString;
	}


	public void setDateLimitString(String dateLimitString) {
		this.dateLimitString = dateLimitString;
	}


	public String getReleaseDateString() {
		return releaseDateString;
	}


	public void setReleaseDateString(String releaseDateString) {
		this.releaseDateString = releaseDateString;
	}
}
