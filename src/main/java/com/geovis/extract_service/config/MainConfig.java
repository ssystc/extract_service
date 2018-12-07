package com.geovis.extract_service.config;

import javax.servlet.MultipartConfigElement;

import org.springframework.boot.web.servlet.MultipartConfigFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class MainConfig {
	
	//上传文件大小配置
	@SuppressWarnings("deprecation")
	@Bean
	public MultipartConfigElement multipartConfigElement() {
		MultipartConfigFactory config = new MultipartConfigFactory();
		config.setMaxFileSize("9000MB");
		config.setMaxRequestSize("9000MB");
		return config.createMultipartConfig();
	}
	
}
