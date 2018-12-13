package com.geovis.extract_service.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class CmdUtil {
	
	public static void processCmd(String[] cmd) {
		Process p  = null;
		InputStream is = null;
		BufferedReader br = null;
		try {
			p = new ProcessBuilder(cmd).start();
			is = p.getInputStream();
			br = new BufferedReader(new InputStreamReader(is));
			String temp = null;
			while((temp = br.readLine()) != null) {
				String msg = new String(temp.getBytes("utf-8"), "utf-8");
				System.out.println(msg);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}finally {
			if(is != null) {
				try {
					is.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	public static void main(String[] args) {
		String[] cmd = new String[] {"python", "E:\\test\\test\\test.py"};
		processCmd(cmd);
	}
	
}
