package es.udc.fic.tic.nautilus.util;

import java.util.List;

public class Metadata {
	private String name;
	private String summary;
	private String typeFile;
	private List<String> tags;
	
	public Metadata(String name, String summary, String typeFile,
			List<String> tags) {
		super();
		this.name = name;
		this.summary = summary;
		this.typeFile = typeFile;
		this.tags = tags;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getSummary() {
		return summary;
	}

	public void setSummary(String summary) {
		this.summary = summary;
	}

	public String getTypeFile() {
		return typeFile;
	}

	public void setTypeFile(String typeFile) {
		this.typeFile = typeFile;
	}

	public List<String> getTags() {
		return tags;
	}

	public void setTags(List<String> tags) {
		this.tags = tags;
	}
}