package es.udc.fic.tic.nautilus.connection;

import es.udc.fic.tic.nautilus.util.ModelConstanst.ENCRYPT_ALG;

public class NautilusKey {
	
	private String fileName;
	private String key;
	private ENCRYPT_ALG encryptAlg;
	private String hash;
	private String host;
	private String hostBackup;
	
	public NautilusKey(String fileName, String key, ENCRYPT_ALG encryptAlg
			,String hash, String host, String hostBackup) {
		super();
		this.fileName = fileName;
		this.key = key;
		this.encryptAlg = encryptAlg;
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

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public ENCRYPT_ALG getEncryptAlg() {
		return encryptAlg;
	}

	public void setEncryptAlg(ENCRYPT_ALG encryptAlg) {
		this.encryptAlg = encryptAlg;
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
