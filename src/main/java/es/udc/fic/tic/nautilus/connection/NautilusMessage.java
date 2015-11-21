package es.udc.fic.tic.nautilus.connection;

import java.io.Serializable;

public class NautilusMessage implements Serializable{
	
	private static final long serialVersionUID = 1L;
	private int type;
	private String hash;
	private byte[] content;
	
	/* This constructor will be used to create file request */
	public NautilusMessage(int type, String hash) {
		super();
		this.type = type;
		this.hash = hash;
	}
	
	
	/* This constructor will be used to create a request to save file */
	public NautilusMessage(int type, String hash, byte[] content) {
		super();
		this.type = type;
		this.hash = hash;
		this.content = content;
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
}
