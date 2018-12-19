package com.geovis.extract_service.service;

import org.springframework.data.domain.Page;

import com.geovis.extract_service.entity.TaskEntity;

public interface TaskService {
	
	/**
	 * 保存任务
	 * @param TaskEntity
	 * @return
	 */
	TaskEntity saveTaskEntity(TaskEntity taskEntity);
	
	
	/**
	 * 更新任务
	 * @param taskEntity
	 * @return
	 */
	TaskEntity updateTaskEntity(TaskEntity taskEntity);
	
	
	/**
	 * 根据id获取任务
	 * @param id
	 * @return
	 */
	TaskEntity getTaskEntityById(Long id);
	
	/**
	 * 添加xpixels字段和ypixels字段
	 * @param id
	 * @param x
	 * @param y
	 */
	void addXY(long id, int x, int y);
	
	
	/**
	 * 根据id获取实体并修改status的值
	 * @param id
	 * @param status
	 */
	void updateStatus(long id, int status);
	
	/**
	 * 根据id添加最终shp结果地址
	 * @param id
	 * @param shpResultPath
	 */
	void addShpResultPath(long id, String shpResultPath);
	
	/**
	 * 分页查询所有
	 */
	Page<TaskEntity> findAllByPage(int pageNum, int pageSize);
}
