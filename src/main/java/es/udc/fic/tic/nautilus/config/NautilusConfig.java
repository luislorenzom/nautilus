package es.udc.fic.tic.nautilus.config;

import java.util.List;

import es.udc.fic.tic.nautilus.util.ModelConstanst;
import es.udc.fic.tic.nautilus.util.ModelConstanst.LANGUAGE;

public class NautilusConfig {

	private boolean serverAvailable;
	private long limitSpace;
	private ModelConstanst.LANGUAGE language;
	private List<ServerInfo> serverPreferences;
	private String storageFolder;
	
	
	public NautilusConfig(boolean serverAvailable, long limitSpace, LANGUAGE language,
			List<ServerInfo> serverPreferences, String storageFolder) {
		super();
		this.serverAvailable = serverAvailable;
		this.limitSpace = limitSpace;
		this.language = language;
		this.serverPreferences = serverPreferences;
		this.storageFolder = storageFolder;
	}
	
	
	public boolean isServerAvailable() {
		return serverAvailable;
	}
	
	public void setServerAvailable(boolean serverAvailable) {
		this.serverAvailable = serverAvailable;
	}
	
	public long getLimitSpace() {
		return limitSpace;
	}
	
	public void setLimitSpace(long limitSpace) {
		this.limitSpace = limitSpace;
	}
	
	public ModelConstanst.LANGUAGE getLanguage() {
		return language;
	}
	
	public void setLanguage(ModelConstanst.LANGUAGE language) {
		this.language = language;
	}
	
	public List<ServerInfo> getServerPreferences() {
		return serverPreferences;
	}
	
	public void setServerPreferences(List<ServerInfo> serverPreferences) {
		this.serverPreferences = serverPreferences;
	}

	public String getStorageFolder() {
		return storageFolder;
	}

	public void setStorageFolder(String storageFolder) {
		this.storageFolder = storageFolder;
	}
}
