package com.geovis.extract_service.task;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.geovis.extract_service.entity.TaskEntity;
import com.geovis.extract_service.service.TaskServiceImpl;
import com.geovis.extract_service.util.CmdUtil;
import com.geovis.extract_service.util.ZipUtil;


@Component("extractFromPngByFcnTask")
@Scope("prototype")
public class ExtractFromPngsByFcnTaskImpl implements Task {
	
	@Autowired
	private TaskServiceImpl taskServiceImpl;
	
	@Value("${extract_result.file.location}")
	private String extractResultLocation;
	
	@Value("${fcn.pythonfile.location}")
	private String fcnLocation;
	
	private Long taskId;
	
	public void setTaskId(Long taskId) {
		this.taskId = taskId;
	}
	
	public Long getTaskId() {
		return taskId;
	}
	
	private List<String> uploadFileNamesList = new ArrayList<>();

	@Override
	public void startRun() {
		Long taskId = this.getTaskId();
		taskServiceImpl.updateStatus(taskId, TaskStatus.Ready.getCode());
		TaskEntity taskEntity = taskServiceImpl.getTaskEntityById(taskId);
		String uploadFilesDir = taskEntity.getUploadFilesDir();				//一次post请求，上传多个文件，放在一个文件夹中
		for (File uploadFile : new File(uploadFilesDir).listFiles()) {
			uploadFileNamesList.add(uploadFile.getName());
			System.out.println(uploadFile.getName());
		}
		
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
		System.out.println("将影响的分辨率读入数据库succ");

		//将以前的过程文件删除
		String pngDir = fcnLocation + "Data_zoo/MIT_SceneParsing/ADEChallengeData2016/images/test/";
		String annotationDir = fcnLocation + "Data_zoo/MIT_SceneParsing/ADEChallengeData2016/annotations/test/";
		String pickelFilePath = fcnLocation + "Data_zoo/MIT_SceneParsing/MITSceneParsing.pickle";
		File pickelFile = new File(pickelFilePath);
		pickelFile.delete();
		for (File annFile : new File(annotationDir).listFiles()) {
			annFile.delete();
		}
		for (File pngFile : new File(pngDir).listFiles()) {
			pngFile.delete();
		}
		for (File uploadFile : new File(uploadFilesDir).listFiles()) {
			String fileNameWithoutDir = uploadFile.getName();
			File toFile1 = new File(pngDir + fileNameWithoutDir);
			File toFile2 = new File(annotationDir + fileNameWithoutDir);
			try {
				Files.copy(uploadFile.toPath(), toFile1.toPath());
				Files.copy(uploadFile.toPath(), toFile2.toPath());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		//启动FCN进行提取
		taskServiceImpl.updateStatus(taskId, TaskStatus.Extracting.getCode());
		File preFileDir = new File(fcnLocation + "pres");
		File tifPresDir = new File(fcnLocation + "pres" + "/tifFilesDir/");
		if (!preFileDir.exists()) {
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
		String fcnMainLocation = fcnLocation + "startClient.py";
		String[] fcnCmd = new String[] {"python", fcnMainLocation};
		CmdUtil.processCmd(fcnCmd);
		System.out.println("__________________FCN extract succ____________");
		
		//预测文件resize
		String resizePyFile = fcnLocation + "imageResize.py";
		String preFileDirName = fcnLocation + "pres/";
		String[] resizeCmd = new String[] {"python", resizePyFile, preFileDirName, x+"", y+""};
		CmdUtil.processCmd(resizeCmd);
		System.out.println("__________________resize succ_________________");
		
		//最终结果矢量化
		taskServiceImpl.updateStatus(taskId, TaskStatus.Polygonizing.getCode());
		String polygonizePyFile = fcnLocation + "pngToTif.py";
		String pngFilesDir = preFileDirName;
		String tifFilesDir = preFileDirName + "tifFilesDir" + File.separator;
		new File(tifFilesDir).mkdirs();
		String shpFilesDir = extractResultLocation + taskId + File.separator;
		new File(shpFilesDir).mkdirs();
		String[] polygonizeCmd = new String[] {"python", polygonizePyFile, pngFilesDir, tifFilesDir, x+"", y+"", shpFilesDir};
		for (String aString : polygonizeCmd) {
			System.out.println(aString);
		}
		CmdUtil.processCmd(polygonizeCmd);
		
		//最终矢量结果压缩
		try {
			ZipUtil.zipShpFile(shpFilesDir);
		} catch (IOException e) {
			e.printStackTrace();
		}
		String shpResultPath = shpFilesDir + "exact.zip";
		taskServiceImpl.addShpResultPath(taskId, shpResultPath);
		taskServiceImpl.updateStatus(taskId, TaskStatus.Complete.getCode());
		
	}
}
