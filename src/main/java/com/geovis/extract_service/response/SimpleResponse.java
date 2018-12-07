package com.geovis.extract_service.response;

public class SimpleResponse {
	
	private int statusCode;
	private String statusMsg;
	private long taskId;
	
	public SimpleResponse() {
		
	}
	
	public SimpleResponse(int statusCode, String statusMsg, long taskId) {
		this.statusCode = statusCode;
		this.statusMsg = statusMsg;
		this.taskId = taskId;
	}
	
	public SimpleResponse(int statusCode, String statusMsg) {
		this.statusCode = statusCode;
		this.statusMsg = statusMsg;
	}

	public int getStatusCode() {
		return statusCode;
	}

	public void setStatusCode(int statusCode) {
		this.statusCode = statusCode;
	}

	public String getStatusMsg() {
		return statusMsg;
	}

	public void setStatusMsg(String statusMsg) {
		this.statusMsg = statusMsg;
	}

	public long getTaskId() {
		return taskId;
	}

	public void setTaskId(long taskId) {
		this.taskId = taskId;
	}

}
