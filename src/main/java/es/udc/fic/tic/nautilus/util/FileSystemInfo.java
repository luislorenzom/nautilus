package es.udc.fic.tic.nautilus.util;

public class FileSystemInfo {
	
	private long used;
	private long avaliable;
	private long total;
	private long percentage;
	private String name;
	private String sysType;
	
	public FileSystemInfo(long used, long avaliable, long total, long percentage,
			String name, String sysType) {
		super();
		this.used = used;
		this.avaliable = avaliable;
		this.total = total;
		this.percentage = percentage;
		this.name = name;
		this.sysType = sysType;
	}
	
	public long getUsed() {
		return used;
	}

	public void setUsed(long used) {
		this.used = used;
	}

	public long getAvaliable() {
		return avaliable;
	}

	public void setAvaliable(long avaliable) {
		this.avaliable = avaliable;
	}

	public long getTotal() {
		return total;
	}

	public void setTotal(long total) {
		this.total = total;
	}

	public long getPercentage() {
		return percentage;
	}

	public void setPercentage(long percentage) {
		this.percentage = percentage;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getSysType() {
		return sysType;
	}

	public void setSysType(String sysType) {
		this.sysType = sysType;
	}
	
	@Override
	public String toString() {
		return "SystemInfo "+this.name+ "[used="+this.used+", avaliable="+this.avaliable+
			   "total="+this.total+"percentageUsed"+this.percentage+"SystemType="+this.sysType+"]";
	}
}