package com.geovis.extract_service.task;

public enum TaskStatus {

	Complete(0, "complete success"),
	Ready(1, "upload success, server is ready to process task, please wait later"),
	Extracting(2, "extract by brain net"),
	Polygonizing(3, "polygonizing"),
	UploadError(4, "upload error"),
	ExtractError(5, "extract error"),
	PloygonizedError(6, "polygonized error"),
	FilesHasDiffResolutionError(7, "upload files have different resolution, please check or find error file name in log");
	
	private int code;
	private String message;
	
	private TaskStatus(int code, String message) {
		this.code = code;
		this.message = message;
	}
	
	public int getCode() {
		return code;
	}
	
	public String getMessage() {
		return message;
	}
	
	/**
     * 根据code获取message
     * @param code
     * @return
     */
    public static String getMessageByCode(int code){
        for(TaskStatus taskStatus : TaskStatus.values()){
            if(code == taskStatus.getCode()){
                return taskStatus.getMessage();
            }
        }
        return  null;
    }
	
}
