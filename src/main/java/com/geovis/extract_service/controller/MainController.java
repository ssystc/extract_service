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
import com.geovis.extract_service.task.FangT;
import com.geovis.extract_service.task.TaskStatus;
import com.geovis.extract_service.task.TestTaskImpl;
import com.geovis.extract_service.task.manager.TaskManagerThread;



@RestController
public class MainController {
	
	@Autowired
	private FangT fangt;
	
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
	
//	@PostMapping("/uploadtif_old")
//	public SimpleResponse uploadTifFile(@RequestParam("file") MultipartFile file) {
//		try {
//			String fileDir = uploadLocation + UUID.randomUUID().toString() + File.separator;
//			File filesDirFile = new File(fileDir);
//			filesDirFile.mkdirs();
//			
//			String filename = file.getOriginalFilename();
//			String destFileName = fileDir + filename;
//			File dest = new File(destFileName);
//			file.transferTo(dest);
//			
//			TaskEntity task = new TaskEntity(destFileName);
//			taskServiceImpl.saveTaskEntity(task);
//			Long id = task.getId();
//			ExtractFromTifByFcnTaskImpl extractFromTifByFcnTaskImpl = (ExtractFromTifByFcnTaskImpl)ExtractApplicationContext.getBean("extractFromTifByFcnTask");
//			extractFromTifByFcnTaskImpl.setTaskId(id);
//			taskManagerThread.addTask(extractFromTifByFcnTaskImpl);
//			
//			return new SimpleResponse(TaskStatus.Ready.getCode(), TaskStatus.Ready.getMessage(), id);
//		} catch (Exception e) {
//			return new SimpleResponse(TaskStatus.UploadError.getCode(), TaskStatus.UploadError.getMessage());
//		}
//	}
	
	@PostMapping("/upload_old")
    public SimpleResponse uploadFiles(@RequestParam("files") MultipartFile[] files){
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
			ExtractFromPngsByFcnTaskImpl extractFromPngsByFcnTaskImpl = (ExtractFromPngsByFcnTaskImpl)ExtractApplicationContext.getBean("extractFromPngByFcnTask");
			extractFromPngsByFcnTaskImpl.setTaskId(id);
			taskManagerThread.addTask(extractFromPngsByFcnTaskImpl);
			
			return new SimpleResponse(TaskStatus.Ready.getCode(), TaskStatus.Ready.getMessage(), id);
		} catch (IllegalStateException | IOException e) {
			return new SimpleResponse(TaskStatus.UploadError.getCode(), TaskStatus.UploadError.getMessage());
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
		
		
//		Set<Integer> errors = TaskStatus.getErrorsSet();
//
//		TaskEntity taskEntity = taskServiceImpl.getTaskEntityById(taskId);
//		
//		while(true) {		
//			if(errors.contains(taskEntity.getStatus())) {
//				return new SimpleResponse(taskEntity.getStatus(), "error", taskId);
//			}
//			
//			if(taskServiceImpl.getTaskEntityById(taskId).getStatus() == TaskStatus.Complete.getCode()) {
//				break;
//			}
//			
//			else {
//				try {
//					Thread.sleep(10);
//				} catch (InterruptedException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
//			}
//		}
//		try {
//			Thread.sleep(200);
//		} catch (InterruptedException e1) {
//			// TODO Auto-generated catch block
//			e1.printStackTrace();
//		}
//		
//		if(taskServiceImpl.getTaskEntityById(taskId).getStatus() == TaskStatus.Complete.getCode()) {
//			String filePath = taskEntity.getShpResultPath();
//			File file = new File(filePath);
//			if (file.exists()) {
//				response.setContentType("application/force-download");// 设置强制下载不打开
//	            response.addHeader("Content-Disposition", "attachment;fileName=" + "extract.zip");// 设置文件名
//	            byte[] buffer = new byte[1024];
//	            FileInputStream fis = null;
//	            BufferedInputStream bis = null;
//	            try {
//	                fis = new FileInputStream(file);
//	                bis = new BufferedInputStream(fis);
//	                OutputStream os = response.getOutputStream();
//	                int i = bis.read(buffer);
//	                while (i != -1) {
//	                    os.write(buffer, 0, i);
//	                    i = bis.read(buffer);
//	                }
//	            } catch (Exception e) {
//	                e.printStackTrace();
//	            } finally {
//	                if (bis != null) {
//	                    try {
//	                        bis.close();
//	                    } catch (IOException e) {
//	                        e.printStackTrace();
//	                    }
//	                }
//	                if (fis != null) {
//	                    try {
//	                        fis.close();
//	                    } catch (IOException e) {
//	                        e.printStackTrace();
//	                    }
//	                }
//	            }
//			}
//			return null; 
//		} else {
//			return new SimpleResponse(taskEntity.getStatus(), "programming...", taskId);
//		}
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
			
			return new SimpleResponse(TaskStatus.Ready.getCode(), TaskStatus.Ready.getMessage(), id);
		} catch (IllegalStateException | IOException e) {
			return new SimpleResponse(TaskStatus.UploadError.getCode(), TaskStatus.UploadError.getMessage());
		}
    }
	
	@PostMapping("/uploadTif")
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
			
			return new SimpleResponse(TaskStatus.Ready.getCode(), TaskStatus.Ready.getMessage(), id);
		} catch (Exception e) {
			return new SimpleResponse(TaskStatus.UploadError.getCode(), TaskStatus.UploadError.getMessage());
		}
	}
	
	@GetMapping("/fangru")
	public String fangru(@RequestParam("id") Long id, @RequestParam("status") Integer status) {
		fangt.ftest(id, status);
		return "succ";
	}
	
	@GetMapping("/quchu")
	public String quchu() {
		Map<Long, Integer> map = taskInfoBean.getStatusMap();
		String mString = "";
		for(Long idLong : map.keySet()) {
			mString += idLong;
		}
		return mString;
	}

}
