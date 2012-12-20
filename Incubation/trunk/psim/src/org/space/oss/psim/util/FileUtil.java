package org.space.oss.psim.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;


public class FileUtil {
	public static boolean deleteDirectory(File dir) {
		if (dir.isDirectory()) {
			String[] children = dir.list();
			for (int i=0; i<children.length; i++) {
				boolean success = deleteDirectory(new File(dir, children[i]));
				if (!success) {
					return false;
		        }
			}
		}
		// The directory is now empty so delete it
		return dir.delete();
	}
	
	public static boolean createDirectory(File dir) {
		if (dir.exists()) return true;
		if (dir.getParentFile().exists()) return dir.mkdir();
		return createDirectory(dir.getParentFile());
	}
	
	public static byte[] readAsByteArray(File file) throws Exception {
		// make sure file exists and we can read it..
		if (!file.exists()) throw new RuntimeException("File "+file.getName()+" does not exist.");
		if (!file.canRead()) throw new RuntimeException("File "+file.getName()+" does not have read permissions.");
		if (!file.isFile()) throw new RuntimeException("File "+file.getName()+" is either a directory, or not a file that can be read.");

		try {
			if (file.length() > (long)Integer.MAX_VALUE) {
				throw new RuntimeException("File size too big: "+file.length());
			}

			byte[] content = new byte[(int)file.length()];
			FileInputStream fis = new FileInputStream(file);
			fis.read(content);
			fis.close();
			return content;
		} catch (Exception e) {
			if (e instanceof RuntimeException) throw e;
			throw new RuntimeException("Error loading file: "+file.getName());
		}
	}
	
	public static void writeFromByteArray(File file, byte[] content) throws Exception {
		if (file.exists() && (!file.isFile() || !file.canWrite())) throw new RuntimeException("File to be overwritten "+file.getName()+" is either a directory, or not a file that can be written to.");
		
		try {
			file.createNewFile();
			FileOutputStream fos = new FileOutputStream(file);
			fos.write(content);
			fos.flush();
			fos.close();
		} catch (Exception e) {
			if (e instanceof RuntimeException) throw e;
			throw new RuntimeException("Error loading file: "+file.getName());
		}
	}
	
	public static void copyFile(File source, File dest) throws Exception {
		if (!source.exists())  {
			throw new Exception("Source file does not exist");
		}
		if (!source.canRead()) {
			throw new Exception("Can not read source file");
		}
		if (!dest.exists()) {
			dest.createNewFile();
			if (!dest.canWrite()) {
				throw new Exception("Can not write to destination file");
			}
		}
		FileInputStream fis = new FileInputStream(source);
		FileOutputStream fos = new FileOutputStream(dest);
		byte[] buff = new byte[(int)source.length()];
		fis.read(buff);
		fis.close();
		fos.write(buff);
		fos.flush();
		fos.close();
	}
	
	public static File getTempDir() {
		File tempDir = new File(System.getProperty("java.io.tmpdir"));
		if (tempDir.exists()) return tempDir;
		return null;
	}
}
