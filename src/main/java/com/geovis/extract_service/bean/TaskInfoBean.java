package com.geovis.extract_service.bean;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Component;

@Component("taskInfoBean")
public class TaskInfoBean {

	public TaskInfoBean() {
		
	}
	
	private Map<Long, Integer> statusMap = new HashMap<>();

	public Map<Long, Integer> getStatusMap() {
		return statusMap;
	}

	public void setStatusMap(Map<Long, Integer> statusMap) {
		this.statusMap = statusMap;
	}
}
