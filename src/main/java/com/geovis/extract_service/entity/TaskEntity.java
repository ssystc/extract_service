package com.geovis.extract_service.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name="tasks")
public class TaskEntity {

	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private long id;
	
	@Column
	private String uploadFilesDir;

	@Column
	private int status;
	
	@Column
	private String cuttingStatus;
	
	@Column
	private int xPixels;
	
	@Column
	private int yPixels;
	
	@Column
	private String shpResultPath;
	
	public String getShpResultPath() {
		return shpResultPath;
	}

	public void setShpResultPath(String shpResultPath) {
		this.shpResultPath = shpResultPath;
	}

	public int getxPixels() {
		return xPixels;
	}

	public void setxPixels(int xPixels) {
		this.xPixels = xPixels;
	}

	public int getyPixels() {
		return yPixels;
	}

	public void setyPixels(int yPixels) {
		this.yPixels = yPixels;
	}

	public TaskEntity() {
		
	}
	
	public TaskEntity(String uploadFilesDir) {
		this.uploadFilesDir = uploadFilesDir;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getUploadFilesDir() {
		return uploadFilesDir;
	}

	public void setUploadFilesDir(String uploadFilesDir) {
		this.uploadFilesDir = uploadFilesDir;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public String getCuttingStatus() {
		return cuttingStatus;
	}

	public void setCuttingStatus(String cuttingStatus) {
		this.cuttingStatus = cuttingStatus;
	}
	
}
