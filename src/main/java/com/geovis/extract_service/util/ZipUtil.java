package com.geovis.extract_service.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class ZipUtil {
	
	public static void zipShpFile(String ShpDirName) throws IOException {
		ZipOutputStream out = null;
		try {
			File shpDirFile = new File(ShpDirName);
			File[] shpFiles = shpDirFile.listFiles();
			
			
			String zipFileName = ShpDirName + File.separator + "exact.zip";
			File zipFile = new File(zipFileName);
			FileOutputStream os = new FileOutputStream(zipFile);
			out = new ZipOutputStream(os);
			byte[] buffer = new byte[1024];
			
			for (File file : shpFiles) {
				if (file.getName().endsWith(".geojson")) {
					FileInputStream fis = new FileInputStream(file);
					out.putNextEntry(new ZipEntry(file.getName()));
					int len;
					while ((len = fis.read(buffer)) != -1) {
						out.write(buffer, 0, len);
					}
				}
				
			}
		}catch (Exception e) {
			e.printStackTrace();
		}finally {
			if(out!=null) {
				out.close();
			}
		}
		
	}

}
