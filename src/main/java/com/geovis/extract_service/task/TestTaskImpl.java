package com.geovis.extract_service.task;

import java.util.Date;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;


@Component("testTaskImpl")
@Scope("prototype")
public class TestTaskImpl implements Task {

	@Override
	public void startRun() {
		Date date = new Date();
		String dateStr = date.toString();
		System.out.println(dateStr);
		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println(dateStr);
	}
}
