package com.geovis.extract_service.task;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component("extractFromTifByUNetImpl")
@Scope("prototype")
public class ExtractFromTifByUNetImpl implements Task {

	private Long taskId;
	
	public void setTaskId(Long taskId) {
		this.taskId = taskId;
	}
	
	public Long getTaskId() {
		return taskId;
	}
	
	@Override
	public void startRun() {
		// TODO Auto-generated method stub
		
	}

}
