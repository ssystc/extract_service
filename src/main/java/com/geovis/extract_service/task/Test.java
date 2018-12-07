package com.geovis.extract_service.task;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

public class Test {

	public static void main(String[] args) throws IOException {
		File f = new File("C:\\Users\\admin\\Desktop\\390.tif");
//		BufferedImage firstFileBi = ImageIO.read(f);
//		int x = firstFileBi.getWidth();
//		System.out.println(x);
		System.out.println(f.getAbsolutePath());
		
	}
	
}
