package com.geovis.extract_service.task.manager;

import java.util.concurrent.ConcurrentLinkedQueue;

import org.springframework.stereotype.Component;

import com.geovis.extract_service.task.Task;

@Component("taskManagerThread")
public class TaskManagerThread extends Thread {
	
	private ConcurrentLinkedQueue<Task> _taskQueue;
	private boolean _stop = false;
	
	public TaskManagerThread() {
		_taskQueue = new ConcurrentLinkedQueue<>();
	}
	
	public void addTask(Task task) {
		_taskQueue.add(task);
	}
	
	public void setStop() {
		synchronized(_taskQueue) {
			_stop = true;
			_taskQueue.notify();
		}
	}
	
	@Override
	public void run() {
		synchronized(_taskQueue) {
			while(!_stop) {
				Task task = _taskQueue.poll();
				if(task!=null) {
					try {
						task.startRun();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}else {
					try {
						_taskQueue.wait(1000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		}
	}

}
