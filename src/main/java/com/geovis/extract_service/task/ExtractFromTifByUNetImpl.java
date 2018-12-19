package com.geovis.extract_service.task;

import java.io.File;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.geovis.extract_service.entity.TaskEntity;
import com.geovis.extract_service.service.TaskServiceImpl;
import com.geovis.extract_service.util.CmdUtil;
import com.geovis.extract_service.util.ZipUtil;

@Component("extractFromTifByUNetImpl")
@Scope("prototype")
public class ExtractFromTifByUNetImpl implements Task {

private final Logger logger = LoggerFactory.getLogger(getClass());
	
	@Autowired
	private TaskServiceImpl taskServiceImpl;
	
	@Value("${extract_result.file.location}")
	private String extractResultLocation;
	
	@Value("${unet.pythonfile.location}")
	private String unetLocation;
	
	private Long taskId;
	
	public void setTaskId(Long taskId) {
		this.taskId = taskId;
	}
	
	public Long getTaskId() {
		return taskId;
	}
	
	@Override
	public void startRun() {
		Long taskId = this.getTaskId();
		taskServiceImpl.updateStatus(taskId, TaskStatus.Ready.getCode());
		TaskEntity taskEntity = taskServiceImpl.getTaskEntityById(taskId);
		String uploadFilesDir = taskEntity.getUploadFilesDir();				//这个值是tif文件上传后的全路径
		
		//将以前的过程文件删除
		String testDataDir = unetLocation + "test_data/";
		for (File testData : new File(testDataDir).listFiles()) {
			testData.delete();
		}

		//把tif文件切成小块,并写入testDataDir中
		logger.info("tif文件开始切割");
		String splitTifPyFile = unetLocation + "splitTifToPngs.py";
		String[] splitCmd = new String[] {"python", splitTifPyFile, uploadFilesDir, testDataDir, 256+"", 256+""};
		CmdUtil.processCmd(splitCmd);
		logger.info("tif文件完成切割");
		
		//启动U-Net进行提取
		logger.info("U-Net开始提取");
		taskServiceImpl.updateStatus(taskId, TaskStatus.Extracting.getCode());
		File preFileDir = new File(unetLocation + "result/");
		File tifPresDir = new File(unetLocation + "result/" + "tifFilesDir/");
		if(!preFileDir.exists()) {
			preFileDir.mkdirs();
		}
		for(File file : preFileDir.listFiles()) {
			file.delete();
		}
		if(tifPresDir.exists()) {
			for(File tifpreFile : tifPresDir.listFiles()) {
				tifpreFile.delete();
			}
		}
		String unetMainLocation = unetLocation + "startWhileUsePngs.py";
		String[] fcnCmd = new String[] {"python", unetMainLocation, testDataDir};
		CmdUtil.processCmd(fcnCmd);
		logger.info("U-Net提取完成");
		
		//将提取的结果合成原始大小的tif
		logger.info("结果文件合并开始");
		String tifFilesDir = unetLocation + "result/tifFilesDir/";
		new File(tifFilesDir).mkdirs();
		String joinPyFile = unetLocation + "joinPngsToOneTif.py";
		String[] joinCmd = new String[] {"python", joinPyFile, unetLocation + "result/", unetLocation + "result/tifFilesDir/result.tif", uploadFilesDir, 256+"", 256+""};
		CmdUtil.processCmd(joinCmd);
		logger.info("结果文件合并完成");

		//最终结果矢量化
		logger.info("最终结果矢量化开始");
		taskServiceImpl.updateStatus(taskId, TaskStatus.Polygonizing.getCode());
		String polygonizePyFile = unetLocation + "tifToShp.py";
		String shpFilesDir = extractResultLocation + taskId + File.separator;
		new File(shpFilesDir).mkdirs();
		String[] polygonizeCmd = new String[] {"python", polygonizePyFile, tifFilesDir, shpFilesDir};		
		CmdUtil.processCmd(polygonizeCmd);
		logger.info("最终结果矢量化完成");

		//最终矢量结果压缩
		logger.info("矢量结果压缩开始");
		try {
			ZipUtil.zipShpFile(shpFilesDir);
		} catch (IOException e) {
			e.printStackTrace();
		}
		String shpResultPath = shpFilesDir + "exact.zip";
		taskServiceImpl.addShpResultPath(taskId, shpResultPath);
		taskServiceImpl.updateStatus(taskId, TaskStatus.Complete.getCode());
		logger.info("矢量结果压缩完成");
	}

}
