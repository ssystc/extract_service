package com.geovis.extract_service.task;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Map;

import javax.imageio.ImageIO;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RestController;

import com.geovis.extract_service.ExtractApplicationContext;
import com.geovis.extract_service.bean.TaskInfoBean;

@Component
public class FangT {
	
//	TaskInfoBean taskInfoBean = (TaskInfoBean)ExtractApplicationContext.getBean("taskInfoBean");
	@Autowired
	private TaskInfoBean taskInfoBean;
	
	public FangT() {
	}
	
	public void ftest(Long id, Integer status) {
		this.taskInfoBean.getStatusMap().put(id, status);
	}
	
}
