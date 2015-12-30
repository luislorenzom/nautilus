package es.udc.fic.tic.nautilus.connection;

import javax.crypto.SecretKey;

public class NautilusKey {
	
	private String fileName;
	private SecretKey key;
	private String hash;
	private String host;
	private String hostBackup;
	
	public NautilusKey(String fileName, SecretKey key, String hash,
			String host, String hostBackup) {
		super();
		this.fileName = fileName;
		this.key = key;
		this.setHash(hash);
		this.host = host;
		this.hostBackup = hostBackup;
	}
	
	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public SecretKey getKey() {
		return key;
	}

	public void setKey(SecretKey key) {
		this.key = key;
	}

	public String getHost() {
		return host;
	}
	
	public String getHash() {
		return hash;
	}

	public void setHash(String hash) {
		this.hash = hash;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public String getHostBackup() {
		return hostBackup;
	}

	public void setHostBackup(String hostBackup) {
		this.hostBackup = hostBackup;
	}
}
