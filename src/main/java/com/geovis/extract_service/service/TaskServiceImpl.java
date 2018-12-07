package com.geovis.extract_service.service;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.geovis.extract_service.entity.TaskEntity;
import com.geovis.extract_service.respository.TaskRespository;


@Service
public class TaskServiceImpl implements TaskService {

	@Autowired
	private TaskRespository taskRespository;

	@Transactional
	@Override
	public TaskEntity saveTaskEntity(TaskEntity taskEntity) {
		return taskRespository.save(taskEntity);
	}

	@Transactional
	@Override
	public TaskEntity updateTaskEntity(TaskEntity taskEntity) {
		return taskRespository.save(taskEntity);
	}

	@Override
	public TaskEntity getTaskEntityById(Long id) {
		return taskRespository.findOneById(id);
	}

	@Transactional
	@Override
	public void addXY(long id, int x, int y) {
		TaskEntity task = taskRespository.findOneById(id);
		task.setxPixels(x);
		task.setyPixels(y);
		taskRespository.saveAndFlush(task);
	}

	@Transactional
	@Override
	public void updateStatus(long id, int status) {
		TaskEntity task = taskRespository.findOneById(id);
		task.setStatus(status);
		taskRespository.saveAndFlush(task);
	}

	@Transactional
	@Override
	public void addShpResultPath(long id, String shpResultPath) {
		TaskEntity task = taskRespository.findOneById(id);
		task.setShpResultPath(shpResultPath);
		taskRespository.saveAndFlush(task);
		
	}
	
}
