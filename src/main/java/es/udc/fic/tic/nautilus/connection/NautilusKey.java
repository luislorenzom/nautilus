package es.udc.fic.tic.nautilus.connection;

import javax.crypto.SecretKey;

import net.tomp2p.peers.Number160;

public class NautilusKey {
	
	private String fileName;
	private SecretKey key;
	private Number160 host;
	private Number160 hostBackup;
	
	public NautilusKey(String fileName, SecretKey key, Number160 host,
			Number160 hostBackup) {
		super();
		this.fileName = fileName;
		this.key = key;
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

	public Number160 getHost() {
		return host;
	}

	public void setHost(Number160 host) {
		this.host = host;
	}

	public Number160 getHostBackup() {
		return hostBackup;
	}

	public void setHostBackup(Number160 hostBackup) {
		this.hostBackup = hostBackup;
	}
}
