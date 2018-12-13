package com.geovis.extract_service.task;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

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

@Component("extractFromPngsByUNetImpl")
@Scope("prototype")
public class ExtractFromPngsByUNetImpl implements Task {
	private final Logger logger = LoggerFactory.getLogger(getClass());
	
	@Autowired
	private TaskServiceImpl taskServiceImpl;
	
	@Value("${extract_result.file.location}")
	private String extractResultLocation;
	
	@Value("${unet.pythonfile.location}")
	private String unetFileLocation;
	
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
		String uploadFilesDir = taskEntity.getUploadFilesDir();				//一次post请求，上传多个文件，放在一个文件夹中
		
		//将影像的分辨率读入数据库
		int x = 0, y = 0;
		File firstFile = new File(uploadFilesDir).listFiles()[0];
		try {
			BufferedImage firstFileBi = ImageIO.read(firstFile);
			x = firstFileBi.getWidth();
			y = firstFileBi.getHeight();
//			for (File uploadFile : new File(uploadFilesDir).listFiles()) {
//				BufferedImage bi = ImageIO.read(uploadFile);
//				if (x != bi.getWidth() || y != bi.getHeight()) {
//					taskServiceImpl.updateStatus(taskId, TaskStatus.FilesHasDiffResolutionError.getCode());
////					return;
//				}
//			}
			taskServiceImpl.addXY(taskId, x, y);
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		logger.info("影像分辨率读入数据库，x=" + x + ",y=" + y);

		//将以前的过程文件删除,并将上传的文件放入u-net/test_data/文件夹下准备predict
		String testDataDir = unetFileLocation + "test_data/";
		for (File testData : new File(testDataDir).listFiles()) {
			testData.delete();
		}
		for (File uploadFile : new File(uploadFilesDir).listFiles()) {
			String fileNameWithoutDir = uploadFile.getName();
			File toFile = new File(testDataDir + fileNameWithoutDir);
			try {
				Files.copy(uploadFile.toPath(), toFile.toPath());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		//启动UNET进行提取
		logger.info("U-Net extract start");
		taskServiceImpl.updateStatus(taskId, TaskStatus.Extracting.getCode());
		File resultDir = new File(unetFileLocation + "result/");
		File tifPresDir = new File(unetFileLocation + "result/" + "tifFilesDir/");
		if (!resultDir.exists()) {
			resultDir.mkdirs();
		}
		for(File file : resultDir.listFiles()) {
			file.delete();
		}
		if(tifPresDir.exists()) {
			for(File tifpreFile : tifPresDir.listFiles()) {
				tifpreFile.delete();
			}
		}
		String unetMainLocation = unetFileLocation + "startWhileUsePngs.py";
		String[] fcnCmd = new String[] {"python", unetMainLocation, testDataDir};
		CmdUtil.processCmd(fcnCmd);
		logger.info("U-Net extract succ");

		
		//预测文件resize
		String resizePyFile = unetFileLocation + "imageResize.py";
		String presDir = unetFileLocation + "result/";
		if (x!=256 || y!=256) {
			String[] resizeCmd = new String[] {"python", resizePyFile, presDir, x+"", y+""};
			CmdUtil.processCmd(resizeCmd);
		}
		
		
		//最终结果矢量化，并转成geojson
		logger.info("最终结果矢量化开始");
		taskServiceImpl.updateStatus(taskId, TaskStatus.Polygonizing.getCode());
		String polygonizePyFile = unetFileLocation + "pngToShp.py";
		String pngFilesDir = presDir;
		String tifFilesDir = presDir + "tifFilesDir/";
		new File(tifFilesDir).mkdirs();
		String shpFilesDir = extractResultLocation + taskId + File.separator;
		new File(shpFilesDir).mkdirs();
		String[] polygonizeCmd = new String[] {"python", polygonizePyFile, pngFilesDir, tifFilesDir, x+"", y+"", shpFilesDir};
		for (String aString : polygonizeCmd) {
			System.out.println(aString);
		}
		CmdUtil.processCmd(polygonizeCmd);
		logger.info("最终结果矢量化完成");
		
//		//shp文件转geojson
//		logger.info("shp转geojson开始");
//		for (File f : new File(shpFilesDir).listFiles()) {
//			String fileName = f.getAbsolutePath();
//			if (fileName.endsWith(".shp")) {
//				String geoJsonName = fileName.replace(".shp", ".geojson");
//				String[] ogr2ogrCmd = new String[] {"ogr2ogr", "-f", "GeoJson", geoJsonName, fileName};
//				CmdUtil.processCmd(ogr2ogrCmd);
//			}
//		}
//		logger.info("shp转geojson结束");

		
		//最终矢量结果压缩
		logger.info("矢量文件压缩开始");
		try {
			ZipUtil.zipShpFile(shpFilesDir);
		} catch (IOException e) {
			e.printStackTrace();
		}
		String shpResultPath = shpFilesDir + "exact.zip";
		taskServiceImpl.addShpResultPath(taskId, shpResultPath);
		taskServiceImpl.updateStatus(taskId, TaskStatus.Complete.getCode());
		logger.info("矢量文件压缩完成");
	}

}
