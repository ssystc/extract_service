package com.geovis.extract_service.service;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Service;

import com.geovis.extract_service.entity.TaskEntity;
import com.geovis.extract_service.respository.TaskRespository;


@Service
public class TaskServiceImpl implements TaskService {

	@Autowired
	private TaskRespository taskRespository;


	@Transactional
	@Modifying(clearAutomatically = true)
	@Override
	public TaskEntity saveTaskEntity(TaskEntity taskEntity) {
		return taskRespository.save(taskEntity);
	}


	@Transactional
	@Modifying(clearAutomatically = true)
	@Override
	public TaskEntity updateTaskEntity(TaskEntity taskEntity) {
		return taskRespository.save(taskEntity);
	}

	@Override
	public TaskEntity getTaskEntityById(Long id) {
		try {
			TaskEntity taskEntity = taskRespository.findOneById(id);
			return taskEntity;
		} catch (java.lang.NullPointerException e) {
			return null;
		}
		
	}


	@Transactional
	@Modifying(clearAutomatically = true)
	@Override
	public void addXY(long id, int x, int y) {
		TaskEntity task = taskRespository.findOneById(id);
		task.setxPixels(x);
		task.setyPixels(y);
		taskRespository.save(task);
	}


//	@Transactional
//	@Modifying(clearAutomatically = true)
//	@Override
//	public void updateStatus(long id, int status) {
//		TaskEntity task = taskRespository.findOneById(id);
//		task.setStatus(status);
//		taskRespository.save(task);
//	}
	
	
	@Override
	public void updateStatus(long id, int status) {
		taskRespository.updateStatus(id, status);
	}
	
	@Transactional
	@Modifying(clearAutomatically = true)
	@Override
	public void addShpResultPath(long id, String shpResultPath) {
		TaskEntity task = taskRespository.findOneById(id);
		task.setShpResultPath(shpResultPath);
		taskRespository.save(task);
	}
	
	@Override
	public Page<TaskEntity> findAllByPage(int pageNum, int pageSize) {
		pageNum = pageNum-1;
		Sort sort = new Sort(Direction.ASC, "id");
		Pageable pageable = new PageRequest(pageNum, pageSize, sort);
		return taskRespository.findAll(pageable);
	}
	
}
