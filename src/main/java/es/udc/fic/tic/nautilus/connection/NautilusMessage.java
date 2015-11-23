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
	private Calendar releaseDate;
	
	/* This constructor will be used to create file request */
	public NautilusMessage(int type, String hash) {
		super();
		this.type = type;
		this.hash = hash;
	}
	
	
	/* This constructor will be used to create a request to save file */
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
}
