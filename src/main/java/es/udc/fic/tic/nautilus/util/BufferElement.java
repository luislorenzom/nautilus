package es.udc.fic.tic.nautilus.util;

import java.io.Serializable;

import es.udc.fic.tic.nautilus.connection.NautilusMessage;

public class BufferElement implements Serializable {
	
	private static final long serialVersionUID = 1L;
	private NautilusMessage msg;
	private String ipAddress;
	
	
	public BufferElement(NautilusMessage msg, String ipAddress) {
		super();
		this.msg = msg;
		this.ipAddress = ipAddress;
	}

	
	public NautilusMessage getMsg() {
		return msg;
	}

	public void setMsg(NautilusMessage msg) {
		this.msg = msg;
	}

	public String getIpAddress() {
		return ipAddress;
	}

	public void setIpAddress(String ipAddress) {
		this.ipAddress = ipAddress;
	}
}
