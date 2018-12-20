package com.geovis.extract_service.controller;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Map;
import java.util.UUID;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.geovis.extract_service.ExtractApplicationContext;
import com.geovis.extract_service.bean.TaskInfoBean;
import com.geovis.extract_service.entity.TaskEntity;
import com.geovis.extract_service.response.SimpleResponse;
import com.geovis.extract_service.service.TaskServiceImpl;
import com.geovis.extract_service.task.ExtractFromJsonByUnetImpl;
import com.geovis.extract_service.task.ExtractFromPngsByFcnTaskImpl;
import com.geovis.extract_service.task.ExtractFromPngsByUNetImpl;
import com.geovis.extract_service.task.ExtractFromTifByUNetImpl;
import com.geovis.extract_service.task.TaskStatus;
import com.geovis.extract_service.task.TestTaskImpl;
import com.geovis.extract_service.task.manager.TaskManagerThread;



@RestController
public class MainController {
	
	@Autowired
	private TaskInfoBean taskInfoBean;
	
	@Resource(name="taskManagerThread")
	private TaskManagerThread taskManagerThread;
	
	@Autowired
	private TaskServiceImpl taskServiceImpl;
	
	@Value("${upload.file.location}")
	private String uploadLocation;
	
	@GetMapping("/test")
	public String test() {
		TestTaskImpl taskImpl = (TestTaskImpl) ExtractApplicationContext.getBean("testTaskImpl");
		taskManagerThread.addTask(taskImpl);
		return "succ";
	}
	
	@GetMapping(value="/findAll")
	@CrossOrigin
	public Page<TaskEntity> findAll(@RequestParam(name="pageNum", defaultValue="1") int pageNum,
			@RequestParam(name="pageSize", defaultValue="10") int pageSize){
		return taskServiceImpl.findAllByPage(pageNum, pageSize);
	}
	
	@GetMapping(value="/getStatus")
	@CrossOrigin
	public SimpleResponse getStatus(@RequestParam Long taskId) {
		TaskEntity task = taskServiceImpl.getTaskEntityById(taskId);
		if(task==null) {
			return new SimpleResponse(500, "没有该任务");
		}
		return new SimpleResponse(task.getStatus(), TaskStatus.getMessageByCode(task.getStatus()), taskId);
	}
	
	@GetMapping(value="/download_new")
	@CrossOrigin
	public SimpleResponse downloadResult(HttpServletRequest request, HttpServletResponse response, @RequestParam Long taskId) {
		
		while(true) {
			Integer status = taskInfoBean.getStatusMap().get(taskId);
			if (status == TaskStatus.Complete.getCode()) {
				taskInfoBean.getStatusMap().remove(taskId);
				TaskEntity task = taskServiceImpl.getTaskEntityById(taskId);
				String filePath = task.getShpResultPath();
				
				File file = new File(filePath);
				if (file.exists()) {
					response.setContentType("application/force-download");// 设置强制下载不打开
				    response.addHeader("Content-Disposition", "attachment;fileName=" + "extract.zip");// 设置文件名
				    byte[] buffer = new byte[1024];
				    FileInputStream fis = null;
				    BufferedInputStream bis = null;
				    try {
				        fis = new FileInputStream(file);
				        bis = new BufferedInputStream(fis);
				        OutputStream os = response.getOutputStream();
				        int i = bis.read(buffer);
				        while (i != -1) {
				            os.write(buffer, 0, i);
				            i = bis.read(buffer);
				        }
				    } catch (Exception e) {
				        e.printStackTrace();
				    } finally {
				        if (bis != null) {
				            try {
				                bis.close();
				            } catch (IOException e) {
				                e.printStackTrace();
				            }
				        }
				        if (fis != null) {
				            try {
				                fis.close();
				            } catch (IOException e) {
				                e.printStackTrace();
				            }
				        }
				    }
				}
				return null;
			
			}
			else if(TaskStatus.getErrorsSet().contains(status)) {
				return new SimpleResponse(status, TaskStatus.getMessageByCode(status));
			}
			else {
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}	
		}
	}
	
	@GetMapping(value="/download", produces="application/json; charset=UTF-8")
	@CrossOrigin
	public SimpleResponse downloadFile(HttpServletRequest request, HttpServletResponse response, @RequestParam Long taskId) {
		
		String filePath = "/data/dl_data/ownFcn/extract_result/" + taskId + "/exact.zip";
		
		
		while(true) {		
			File f = new File(filePath);
			if(f.exists()) {
				break;
			}
			else {
				try {
					Thread.sleep(10);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		try {
			Thread.sleep(200);
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		File file = new File(filePath);
		if (file.exists()) {
			response.setContentType("application/force-download");// 设置强制下载不打开
		    response.addHeader("Content-Disposition", "attachment;fileName=" + "extract.zip");// 设置文件名
		    byte[] buffer = new byte[1024];
		    FileInputStream fis = null;
		    BufferedInputStream bis = null;
		    try {
		        fis = new FileInputStream(file);
		        bis = new BufferedInputStream(fis);
		        OutputStream os = response.getOutputStream();
		        int i = bis.read(buffer);
		        while (i != -1) {
		            os.write(buffer, 0, i);
		            i = bis.read(buffer);
		        }
		    } catch (Exception e) {
		        e.printStackTrace();
		    } finally {
		        if (bis != null) {
		            try {
		                bis.close();
		            } catch (IOException e) {
		                e.printStackTrace();
		            }
		        }
		        if (fis != null) {
		            try {
		                fis.close();
		            } catch (IOException e) {
		                e.printStackTrace();
		            }
		        }
		    }
		}
		return null;
	}
	
	@PostMapping("/uploadJson")
	@CrossOrigin
	public SimpleResponse uploadJson(@RequestParam("jsonStr") String jsonStr) {
		
		try {
			String message = jsonStr;
			
			String uuid = UUID.randomUUID().toString();
			String fileDir = uploadLocation + uuid + File.separator;
			new File(fileDir).mkdirs();
			String filePath = fileDir + uuid + ".json";
			
			PrintStream ps = null;
			FileOutputStream os = null;
			
			try {
				File file = new File(filePath);
				os = new FileOutputStream(file);
				ps = new PrintStream(os);
				ps.println(message);
			} catch (Exception e) {
				e.printStackTrace();
			}finally {
				ps.close();
				try {
					os.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
			TaskEntity task = new TaskEntity(filePath);
			task.setStatus(TaskStatus.Ready.getCode());
			taskServiceImpl.saveTaskEntity(task);
			Long id = task.getId();
			ExtractFromJsonByUnetImpl extractFromJsonByUnetImpl = (ExtractFromJsonByUnetImpl)ExtractApplicationContext.getBean("extractFromJsonByUnetImpl");
			extractFromJsonByUnetImpl.setTaskId(id);
			taskManagerThread.addTask(extractFromJsonByUnetImpl);
			taskInfoBean.getStatusMap().put(id, TaskStatus.Ready.getCode());
			return new SimpleResponse(TaskStatus.Ready.getCode(), TaskStatus.Ready.getMessage(), id);
		} catch (Exception e) {
			return new SimpleResponse(TaskStatus.UploadError.getCode(), TaskStatus.UploadError.getMessage());
		}
		
	}
	
	
	@PostMapping("/uploadPngs")
	@CrossOrigin
    public SimpleResponse uploadFilesNew(@RequestParam("files") MultipartFile[] files){
    	try {
    		String filesDir = uploadLocation + UUID.randomUUID().toString() + File.separator;
    		File filesDirFile = new File(filesDir);
    		filesDirFile.mkdirs();
    		
			for (int i = 0; i < files.length; i++) {
				String filename = files[i].getOriginalFilename();
				String destFileName = filesDir + filename;
				File dest = new File(destFileName);
				files[i].transferTo(dest);
			}
			
			TaskEntity task = new TaskEntity(filesDir);
			taskServiceImpl.saveTaskEntity(task);
			Long id = task.getId();
			ExtractFromPngsByUNetImpl extractFromPngsByUNetImpl = (ExtractFromPngsByUNetImpl)ExtractApplicationContext.getBean("extractFromPngsByUNetImpl");
			extractFromPngsByUNetImpl.setTaskId(id);
			taskManagerThread.addTask(extractFromPngsByUNetImpl);
			taskInfoBean.getStatusMap().put(id, TaskStatus.Ready.getCode());
			return new SimpleResponse(TaskStatus.Ready.getCode(), TaskStatus.Ready.getMessage(), id);
		} catch (IllegalStateException | IOException e) {
			return new SimpleResponse(TaskStatus.UploadError.getCode(), TaskStatus.UploadError.getMessage());
		}
    }
	
	@PostMapping("/uploadTif")
	@CrossOrigin
	public SimpleResponse uploadTifNew(@RequestParam("file") MultipartFile file) {
		try {
			String fileDir = uploadLocation + UUID.randomUUID().toString() + File.separator;
			File filesDirFile = new File(fileDir);
			filesDirFile.mkdirs();
			
			String filename = file.getOriginalFilename();
			String destFileName = fileDir + filename;
			File dest = new File(destFileName);
			file.transferTo(dest);
			
			TaskEntity task = new TaskEntity(destFileName);
			taskServiceImpl.saveTaskEntity(task);
			Long id = task.getId();
			ExtractFromTifByUNetImpl extractFromTifByUNetImpl = (ExtractFromTifByUNetImpl)ExtractApplicationContext.getBean("extractFromTifByUNetImpl");
			extractFromTifByUNetImpl.setTaskId(id);
			taskManagerThread.addTask(extractFromTifByUNetImpl);
			taskInfoBean.getStatusMap().put(id, TaskStatus.Ready.getCode());
			return new SimpleResponse(TaskStatus.Ready.getCode(), TaskStatus.Ready.getMessage(), id);
		} catch (Exception e) {
			return new SimpleResponse(TaskStatus.UploadError.getCode(), TaskStatus.UploadError.getMessage());
		}
	}

}
