package com.geovis.extract_service.respository;

import javax.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.geovis.extract_service.entity.TaskEntity;

public interface TaskRespository extends JpaRepository<TaskEntity, Long> {

	
	TaskEntity findOneById(Long id);
	
//	@Transactional
//	@Modifying(clearAutomatically=true)
//	@Query("select new com.geovis.extract_service.entity.TaskEntity() from TaskEntity where id=:id")
//	public TaskEntity findOneById(@Param("id")long id);
	
	@Transactional
	@Modifying(clearAutomatically=true)
	@Query("update TaskEntity set status = :status where id=:id")
	public void updateStatus(@Param("id")long id, @Param("status")int status);
	
}
