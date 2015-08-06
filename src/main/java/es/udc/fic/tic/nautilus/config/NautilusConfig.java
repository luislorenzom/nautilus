package es.udc.fic.tic.nautilus.config;

import java.util.List;

import es.udc.fic.tic.nautilus.util.ModelConstanst;
import es.udc.fic.tic.nautilus.util.ModelConstanst.LANGUAGE;

public class NautilusConfig {

	private boolean serverAvailable;
	private Float limitSpace;
	private ModelConstanst.LANGUAGE language;
	private List<ServerInfo> serverPreferences;
	
	
	public NautilusConfig(boolean serverAvailable, Float limitSpace, LANGUAGE language,
			List<ServerInfo> serverPreferences) {
		super();
		this.serverAvailable = serverAvailable;
		this.limitSpace = limitSpace;
		this.language = language;
		this.serverPreferences = serverPreferences;
	}
	
	
	public boolean isServerAvailable() {
		return serverAvailable;
	}
	
	public void setServerAvailable(boolean serverAvailable) {
		this.serverAvailable = serverAvailable;
	}
	
	public Float getLimitSpace() {
		return limitSpace;
	}
	
	public void setLimitSpace(Float limitSpace) {
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
}
