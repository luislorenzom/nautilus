package es.udc.fic.tic.nautilus.util;

import java.util.List;

public class SystemStatistics {
	private double uptime;
	private List<FileSystemInfo> FileSystems;
	private long pingTime;

	/* Empty Constructor */
	public SystemStatistics(){}
	
	/* Constructor with fields */
	public SystemStatistics(double uptime, List<FileSystemInfo> FileSystems, 
			long pingTime) {
		super();
		this.uptime = uptime;
		this.FileSystems = FileSystems;
		this.pingTime = pingTime;
	}
	
	
	public double getUptime() {
		return uptime;
	}

	public void setUptime(double uptime) {
		this.uptime = uptime;
	}

	public List<FileSystemInfo> getFileSystems() {
		return FileSystems;
	}

	public void setFileSystems(List<FileSystemInfo> FileSystems) {
		this.FileSystems = FileSystems;
	}

	public long getPingTime() {
		return pingTime;
	}

	public void setPingTime(long pingTime) {
		this.pingTime = pingTime;
	}
	
	@Override
	public String toString() {
		return "SystemStatistics [uptime="+this.uptime+", pingTime="+this.pingTime
				+"FileSystem{"+this.printFileSystem()+"}]";
	}
	
	/**
	 * @return the String with the File systems info
	 */
	private String printFileSystem() {
		String returnString="\n";
		
		for (FileSystemInfo fileSystemInfo : FileSystems) {
			returnString += fileSystemInfo.toString();
			returnString += "\n";
		}
		return returnString;
	}
}