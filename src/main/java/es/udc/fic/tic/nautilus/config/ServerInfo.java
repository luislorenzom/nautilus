package es.udc.fic.tic.nautilus.config;

public class ServerInfo {
	
	private String name;
	private String hash;
	
	public ServerInfo(String name, String hash) {
		super();
		this.name = name;
		this.hash = hash;
	}

	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getHash() {
		return hash;
	}
	
	public void setHash(String hash) {
		this.hash = hash;
	}
}
