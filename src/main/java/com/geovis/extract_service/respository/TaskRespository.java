package com.geovis.extract_service.respository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.geovis.extract_service.entity.TaskEntity;

public interface TaskRespository extends JpaRepository<TaskEntity, Long> {

	TaskEntity findOneById(Long id);
	
}
