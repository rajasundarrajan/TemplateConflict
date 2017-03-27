package com.Tool.Templateconflict;

public class PriorityDtls {
	String fileName;
	String tmpName;
	Integer priority;
	String mainSS;
	
	
	public String getMainSS() {
		return mainSS;
	}
	public void setMainSS(String mainSS) {
		this.mainSS = mainSS;
	}
	public PriorityDtls(String fileName, String tmpName, Integer priority, String mainSS) {
		super();
		this.fileName = fileName;
		this.tmpName = tmpName;
		this.priority = priority;
		this.mainSS = mainSS;
	}
	public String getFileName() {
		return fileName;
	}
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
	public String getTmpName() {
		return tmpName;
	}
	public void setTmpName(String tmpName) {
		this.tmpName = tmpName;
	}
	public Integer getPriority() {
		return priority;
	}
	public void setPriority(Integer priority) {
		this.priority = priority;
	}
	
	
}
