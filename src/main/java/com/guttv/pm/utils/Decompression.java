package com.guttv.pm.utils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipFile;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

/**
 * 
 * @author Peter
 *
 */
public class Decompression {
	private static final int BUFFEREDSIZE = 1024;
	
	public static List<String> getResource(File jarFile,String regex) throws Exception{
		List<String> paths = new ArrayList<String>();
		
		//Pattern.matches(regex, "");
		
		ZipFile zipFile = null;
		try {
			zipFile = new ZipFile(jarFile);
			Enumeration<ZipArchiveEntry> e = zipFile.getEntries();
			ZipArchiveEntry zipEnt = null;
			while (e.hasMoreElements()) {
				zipEnt = e.nextElement();
				if(Pattern.matches(regex, zipEnt.getName())) {
					paths.add(zipEnt.getName());
				}
			}
			
			
		}finally {
			IOUtils.closeQuietly(zipFile);
		}
		
		return paths;
	}
	
	/**
	 * 解压zip格式的压缩文件到当前文件夹
	 * 
	 * @param zipFileName
	 * @throws Exception
	 */
	public static List<File> unzip(String zipFileName) throws Exception {
		File f = new File(zipFileName);
		return unzip(f, f.getParentFile());
	}

	/**
	 * 解压zip格式的压缩文件到指定位置
	 * 
	 * @param zipFileName
	 *            压缩文件
	 * @param extPlace
	 *            解压目录
	 * @throws Exception
	 */
	public static List<File> unzip(File f, File tempFile) throws Exception {
		ZipFile zipFile = null;
		List<File> files = new ArrayList<File>();
		try {
			zipFile = new ZipFile(f);
			if ((!f.exists()) && (f.length() <= 0)) {
				throw new Exception("要解压的文件不存在!");
			}

			String strPath, gbkPath, strtemp;
			strPath = tempFile.getAbsolutePath();
			FileUtils.forceMkdir(tempFile);
			Enumeration<ZipArchiveEntry> e = zipFile.getEntries();
			while (e.hasMoreElements()) {
				ZipArchiveEntry zipEnt = e.nextElement();
				gbkPath = zipEnt.getName();

				if (zipEnt.isDirectory()) {
					strtemp = strPath + "/" + gbkPath;
					File dir = new File(strtemp);
					FileUtils.forceMkdir(dir);
					continue;
				} else {

					InputStream is = null;
					BufferedInputStream bis = null;
					FileOutputStream fos = null;
					BufferedOutputStream bos = null;
					try {
						// 读写文件
						is = zipFile.getInputStream(zipEnt);
						bis = new BufferedInputStream(is);
						gbkPath = zipEnt.getName();
						strtemp = strPath + "/" + gbkPath;

						// 建目录
						String strsubdir = gbkPath;
						for (int i = 0; i < strsubdir.length(); i++) {
							if (strsubdir.substring(i, i + 1).equalsIgnoreCase("/")) {
								String temp = strPath + "/" + strsubdir.substring(0, i);
								File subdir = new File(temp);
								if (!subdir.exists())
									subdir.mkdir();
							}
						}
						fos = new FileOutputStream(strtemp);
						bos = new BufferedOutputStream(fos);

						int c;
						while ((c = bis.read()) != -1) {
							bos.write((byte) c);
						}
						
						files.add(new File(strtemp));
					} finally {
						IOUtils.closeQuietly(is);
						IOUtils.closeQuietly(bis);
						IOUtils.closeQuietly(bos);
						IOUtils.closeQuietly(fos);
					}

				}
			}
			
			return files;
		} finally {
			IOUtils.closeQuietly(zipFile);
		}
	}

	/**
	 * 压缩zip格式的压缩文件
	 * 
	 * @param inputFilename
	 *            压缩的文件或文件夹及详细路径
	 * @param zipFilename
	 *            输出文件名称及详细路径
	 * @throws IOException
	 */
	public static void zip(String inputFilename, String zipFilename) throws IOException {
		zip(new File(inputFilename), zipFilename);
	}

	/**
	 * 压缩zip格式的压缩文件
	 * 
	 * @param inputFile
	 *            需压缩文件
	 * @param zipFilename
	 *            输出文件及详细路径
	 * @throws IOException
	 */
	public static void zip(File inputFile, String zipFilename) throws IOException {
		ZipOutputStream out = null;
		try {
			out = new ZipOutputStream(new FileOutputStream(zipFilename));
			zip(inputFile, out, "");
		} finally {
			IOUtils.closeQuietly(out);
		}
	}

	/**
	 * 压缩zip格式的压缩文件
	 * 
	 * @param inputFile
	 *            需压缩文件
	 * @param out
	 *            输出压缩文件
	 * @param base
	 *            结束标识
	 * @throws IOException
	 */
	private static void zip(File inputFile, ZipOutputStream out, String base) throws IOException {
		if (inputFile.isDirectory()) {
			File[] inputFiles = inputFile.listFiles();
			out.putNextEntry(new ZipEntry(base + "/"));
			base = base.length() == 0 ? "" : base + "/";
			for (int i = 0; i < inputFiles.length; i++) {
				zip(inputFiles[i], out, base + inputFiles[i].getName());
			}
		} else {
			if (base.length() > 0) {
				out.putNextEntry(new ZipEntry(base));
			} else {
				out.putNextEntry(new ZipEntry(inputFile.getName()));
			}
			FileInputStream in = null;
			try {
				in = new FileInputStream(inputFile);
				int c;
				byte[] by = new byte[BUFFEREDSIZE];
				while ((c = in.read(by)) != -1) {
					out.write(by, 0, c);
				}
			} finally {
				IOUtils.closeQuietly(in);
			}
		}
	}

	//
	public static void main(String[] args) {
		try {
			List<File> files = Decompression.unzip("D:\\data\\task\\task-word.zip");
			for(File file : files) {
				System.out.println(file.getAbsolutePath());
			}
			
			///Decompression.unzip(new File("D:\\data\\task\\task-word.zip"), new File("D:\\data\\task\\task\\word"));
			///Decompression.zip("D:\\data\\task\\task\\word", "D:\\data\\task\\task.zip");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
