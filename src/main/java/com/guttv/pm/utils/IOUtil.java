/**
 * 
 */
package com.guttv.pm.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

/**
 * @author Peter
 *
 */
public class IOUtil {

	/**
	 * 默认重试次数：3
	 * @param is
	 * @param destPath
	 * @throws Exception
	 */
	public static void downLoadForRetry(InputStream is,String destPath) throws Exception{
		downLoadForRetry(is,destPath,3);
	}
	/**
	 * 
	 * @param is 输入流
	 * @param destPath 目的路径
	 * @param retryNum 重试次数
	 * @throws Exception
	 */
	public static void downLoadForRetry(InputStream is,String destPath,int retryNum) throws Exception{
		if(retryNum<=0){
			retryNum=1;
		}
		

		//如果没有成功，重试几次
		boolean success = false;
		Exception ex = null;
		for(int i = 0; i < retryNum && !success; i++) {
			FileOutputStream fos = new FileOutputStream(destPath);
			try {
				IOUtils.copy(is, fos);
				success = true;
			} catch (Exception e) {
				ex = e;
			}finally {
				IOUtils.closeQuietly(fos);
			}
		}
		
		//关闭流
		
		//如果没有成功，抛出异常
		if(!success && ex != null) {
			throw new Exception("重复下载["+retryNum+"]次后失败",ex);
		}
	}
	
	/**
	 * 默认重试次数：3，默认连接超时读取超时时间为：30000
	 * @param url
	 * @param destPath
	 * @throws Exception
	 */
	public static void downLoadForRetry(URL url,String destPath) throws Exception{
		downLoadForRetry(url,destPath,3,30000,30000);
	}
	
	/**
	 * 把本地文件上传到FTP
	 * @param local
	 * @param u
	 * @throws Exception
	 */
	public static boolean uploadToFTPForRetry(String local,String u) throws Exception{
		URL url = new URL(u);
		boolean success = false;
		Exception e = null;
		for (int i = 0; i < 3 && !success; i++) {
			FtpUtil ftpUtil = null;
			try {
				ftpUtil = FtpUtil.getFtpUtil(u);
				ftpUtil.login();
				success = ftpUtil.upFile(local, url.getPath());
			} catch (Exception ex){
				e = ex;
			}finally {
				if (ftpUtil != null) {
					try {
						ftpUtil.logout();
					} catch (Exception e1) {
					}
					ftpUtil = null;
				}
			}
		}
		if(!success && e != null) {
			throw e;
		}
		return success;
	}
	
	/**
	 * 把内容上传到FTP
	 * @param content
	 * @param u
	 * @throws Exception
	 */
	public static boolean uploadToFTPForRetry(byte[] content,String u) throws Exception{
		URL url = new URL(u);
		boolean success = false;
		Exception e = null;
		for (int i = 0; i < 3 && !success; i++) {
			FtpUtil ftpUtil = null;
			try {
				ftpUtil = FtpUtil.getFtpUtil(u);
				ftpUtil.login();
				ftpUtil.upFile(content, url.getPath());
				success = true;
			} catch (Exception ex){
				e = ex;
			}finally {
				if (ftpUtil != null) {
					try {
						ftpUtil.logout();
					} catch (Exception e1) {
					}
					ftpUtil = null;
				}
			}
		}
		if(!success && e != null) {
			throw e;
		}
		return success;
	}
	
	/**
	 * 
	 * @param url
	 * @param destPath
	 * @param retryNum
	 * @param connectTimeout
	 * @param readTimeout
	 * @throws Exception
	 */
	public static void downLoadForRetry(URL url,String destPath,int retryNum,int connectTimeout,int readTimeout) throws Exception{
		if(retryNum<=0){
			retryNum=1;
		}

		//如果没有成功，重试几次
		boolean success = false;
		Exception ex = null;
		for(int i = 0; i < retryNum && !success; i++) {
			try {
				FileUtils.copyURLToFile(url, new File(destPath), connectTimeout, readTimeout);
				success = true;
			} catch (Exception e) {
				ex = e;
			}
		}
		
		//如果没有成功，抛出异常
		if(!success && ex != null) {
			throw new Exception("URL["+url.getPath()+"]重复下载["+retryNum+"]次后失败",ex);
		}
	}
	
	/**
	 * 把URL中的内容读到内存中 默认连接超时和读取超时值为：30000  ，默认重试次数：3
	 * @param url
	 * @param filter 行过滤
	 * @return
	 * @throws Exception
	 */
	public static List<String> readLinesForRetry(URL url,ContentFilter filter)throws Exception{
		return readLinesForRetry(url,3,30000,30000,filter);
	}
	
	/**
	 * 把URL中的内容读到内存中 默认连接超时和读取超时值为：30000  ，默认重试次数：3
	 * @param url
	 * @return
	 * @throws Exception
	 */
	public static List<String> readLinesForRetry(URL url)throws Exception{
		return readLinesForRetry(url,3,30000,30000,null);
	}
	
	/**
	 * 把URL中的内容读到内存中
	 * @param url
	 * @param retryNum
	 * @param connectTimeout
	 * @param readTimeout
	 * @param fielter 内容过滤
	 * @return
	 * @throws Exception
	 */
	public static List<String> readLinesForRetry(URL url,int retryNum,int connectTimeout,int readTimeout,ContentFilter filter)throws Exception{
		if(retryNum<=0){
			retryNum=1;
		}

		//如果没有成功，重试几次
		boolean success = false;
		List<String> lines = null;
		Exception ex = null;
		for(int i = 0; i < retryNum && !success; i++) {
			BufferedReader reader = null;
			String line = null;
			try {
				URLConnection connection = url.openConnection();
				connection.setConnectTimeout(connectTimeout);
				connection.setReadTimeout(readTimeout);
				
				lines = new ArrayList<String>();
				reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
				
				while ((line = reader.readLine()) != null) {
					if(filter == null || filter.filter(line)) {
						lines.add(line);
					}
				}
				success = true;
			} catch (Exception e) {
				ex = e;
			}finally {
				IOUtils.closeQuietly(reader);
			}
		}
		
		//如果没有成功，抛出异常
		if(!success && ex != null) {
			throw new Exception("重复读取["+retryNum+"]次后失败",ex);
		}
		return lines;
	}
	
	public interface ContentFilter{
		/**
		 * 把不需要的行 返回false
		 * @param line
		 * @return
		 */
		public boolean filter(String line);
	}
	
	/**
	 * 读数据
	 * @param reader
	 * @return
	 * @throws IOException
	 */
	public static byte[] readData(InputStream reader) throws IOException{
		try {
			int bufferSize=1024*8 ;

			List<Byte> list=new ArrayList<Byte>(bufferSize);
			int read=-1;
			while ( (read=reader.read()) != -1 ) {
				list.add((byte)read);
			}
			
			byte[] b3 = new byte[list.size()];
			 
			for (int j = 0; j < b3.length; j++) {
				b3[j]=((Byte)list.get(j)).byteValue();
			}

			return b3;
		} finally{
			IOUtils.closeQuietly(reader);
		}
	}
	
	public static String read(URL url) throws IOException{
		StringWriter stringWriter = new StringWriter();
		//重新下载几次
		boolean retry = true;
		int retryNum = 3;
		IOException ex = null;
		for(int i = 0; i < retryNum&&retry; i++) {
			InputStream is = null;
			try {
				URLConnection connection = url.openConnection();
				connection.setConnectTimeout(30000);
				connection.setReadTimeout(30000);
				is = connection.getInputStream();
				IOUtils.copy(is, stringWriter,Constants.ENCODING);
				retry = false;
				break;
			}catch(IOException e) {
				ex = e;
			}finally {
				IOUtils.closeQuietly(is);
			}
		}
		
		if(retry) {
			throw ex;
		}
		return stringWriter.toString();
	}
}
