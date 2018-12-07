package com.geovis.extract_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

import com.geovis.extract_service.task.manager.TaskManagerThread;

@SpringBootApplication
public class ExtractServiceApplication {

	public static ConfigurableApplicationContext configurableApplicationContext;
	
	public static void main(String[] args) {
		configurableApplicationContext = SpringApplication.run(ExtractServiceApplication.class, args);
		TaskManagerThread thread = (TaskManagerThread) configurableApplicationContext.getBean("taskManagerThread");
        thread.start();
	}
	
}
