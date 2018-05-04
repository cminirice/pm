package com.guttv.pm.utils;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPListParseEngine;

import java.io.*;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Peter
 * 
 */
public class FtpUtil {

	protected static Log log = LogFactory.getLog(FtpUtil.class);
	private FTPClient ftpclient;
	private String ipAddress;
	private int ipPort;
	private String userName;
	private String PassWord;
	private String filePath;

	public FtpUtil(String ip, int port, String username, String password)
			throws Exception {
		this.ipAddress = new String(ip);
		this.ipPort = port;
		this.ftpclient = new FTPClient();
		this.ftpclient.setDefaultPort(this.ipPort);
		this.ftpclient.setConnectTimeout(60000);
		this.ftpclient.connect(this.ipAddress);
		this.ftpclient.setDataTimeout(60000);
		this.ftpclient.setSoTimeout(60000);

		this.userName = new String(username);
		this.PassWord = new String(password);
		this.ftpclient.enterLocalPassiveMode();
	}

	public FTPClient getFtpclient() {
		return this.ftpclient;
	}

	public FtpUtil(String ip, String username, String password)
			throws Exception {
		this(ip, 21, username, password);
	}

	public static FtpUtil getFtpUtil(String ftpaddress) throws Exception {
		String[] values = getSingleMatchValue(ftpaddress);
		FtpUtil ftpUtil = new FtpUtil(values[2], Integer.parseInt(values[3]),
				values[0], values[1]);
		ftpUtil.filePath = values[4];
		return ftpUtil;
	}

	public void login() throws IOException {
		this.ftpclient.login(this.userName, this.PassWord);
		this.ftpclient.enterLocalPassiveMode();
		this.ftpclient.setFileType(2);
		this.ftpclient.setFileTransferMode(10);
	}

	public void logout(){
		if (this.ftpclient != null) {
			if (this.ftpclient.isConnected())
				try {
					this.ftpclient.disconnect();
				} catch (Exception e) {
				}
			try {
				this.ftpclient.logout();
			} catch (Exception e) {
			}
		}
	}

	public boolean changeDir(String pathDir) throws IOException {
		return this.ftpclient.changeWorkingDirectory(pathDir);
	}

	public long getFileSize(String fullFilePath) {
		try {
			FTPListParseEngine engine = this.ftpclient
					.initiateListParsing(fullFilePath);
			while (engine.hasNext()) {
				FTPFile[] files = engine.getNext(5);
				int i = 0;
				if (i >= files.length)
					continue;
				long size = files[i].getSize();
				return size / 1024L;
			}
		} catch (IOException e) {
			log.error(e, e);
		}
		return 0L;
	}

	public int deletePath(String fullFilePath) {
		int result = -1;
		try {
			FTPListParseEngine engine = this.ftpclient
					.initiateListParsing(fullFilePath);
			while (engine.hasNext()) {
				FTPFile[] files = engine.getNext(5);

				for (int i = 0; i < files.length; i++) {
					if ((files[i].isDirectory())
							&& (!files[i].getName().equals("."))
							&& (!files[i].getName().equals("..")))
						deletePath(fullFilePath + "/" + files[i].getName());
					else {
						this.ftpclient.deleteFile(fullFilePath + "/"
								+ files[i].getName());
					}
				}
			}

			result = this.ftpclient.sendCommand("RMD " + fullFilePath);
		} catch (IOException e) {
			log.error(e, e);
		}
		return result;
	}

	public long getDirSize(String fullFilePath) {
		long size = 0L;
		try {
			FTPListParseEngine engine = this.ftpclient
					.initiateListParsing(fullFilePath);
			while (engine.hasNext()) {
				FTPFile[] files = engine.getNext(5);

				for (int i = 0; i < files.length; i++) {
					if ((files[i].isDirectory())
							&& (!files[i].getName().equals("."))
							&& (!files[i].getName().equals(".."))) {
						size += getDirSize(fullFilePath + "/"
								+ files[i].getName());
					} else
						size += files[i].getSize();
				}
			}
		} catch (IOException e) {
			log.error(e, e);
		}
		return size;
	}

	public void buildList(String pathList) throws Exception {
		pathList = checkName(pathList);
		String[] paths = pathList.split("/");
		for (int i = 0; i < paths.length; i++) {
			this.ftpclient.makeDirectory(paths[i]);
			this.ftpclient.changeWorkingDirectory(paths[i]);
		}
	}

	public boolean isExistFilePath(String fullPath) throws Exception {
		String parentPath = fullPath.substring(0, fullPath.lastIndexOf('/'));
		String pathName = fullPath.substring(fullPath.lastIndexOf('/') + 1);
		parentPath = checkName(parentPath);

		List<String> namesList = getDirs(parentPath);
		return namesList.contains(pathName);
	}

	public boolean isExistFileName(String fullName) throws Exception {
		String parentPath = fullName.substring(0, fullName.lastIndexOf('/'));
		String pathName = fullName.substring(fullName.lastIndexOf('/') + 1);
		parentPath = checkName(parentPath);
		List<String> namesList = fileNames(parentPath);
		return namesList.contains(pathName);
	}

	public List<String> getDirs(String fullPath) throws Exception {
		fullPath = checkName(fullPath);
		List<String> namesList = new ArrayList<String>();
		FTPFile[] names = this.ftpclient.listDirectories(fullPath);
		if (names != null) {
			for (int i = 0; i < names.length; i++) {
				namesList.add(names[i].getName());
			}
		}
		return namesList;
	}

	public List<String> fileNames(String fullPath) throws Exception {
		fullPath = checkName(fullPath);
		List<String> namesList = new ArrayList<String>();
		String[] names = this.ftpclient.listNames(fullPath);
		if (names != null) {
			for (int i = 0; i < names.length; i++) {
				namesList.add(names[i].toLowerCase());
			}
		}
		return namesList;
	}

	public List<String> getDirectoryFiles(String fullPath) throws Exception {
		fullPath = checkName(fullPath);
		List<String> namesList = new ArrayList<String>();
		String[] names = this.ftpclient.listNames(fullPath);
		if (names != null) {
			for (int i = 0; i < names.length; i++) {
				if (!names[i].endsWith(".xml"))
					continue;
				namesList.add(names[i]);
			}
		}

		return namesList;
	}

	public FTPFile[] getFiles(String fullPath) throws Exception {
		fullPath = checkName(fullPath);
		FTPFile[] ftpFiles = this.ftpclient.listFiles(fullPath);
		return ftpFiles;
	}

	public boolean upFile(String source, String destination) throws Exception {
		destination = checkName(destination);
		if ((destination.split("/").length > 2)
				&& (!isExistFilePath(destination.substring(0,
						destination.lastIndexOf('/'))))) {
			buildList(destination.substring(0, destination.lastIndexOf('/')));
		}

		FileInputStream ftpIn = new FileInputStream(source);
		try {
			return this.ftpclient.storeFile(destination, ftpIn);
		} finally {
			IOUtils.closeQuietly(ftpIn);
		}
	}

	public void upFile(byte[] sourceData, String destination) throws Exception {
		destination = checkName(destination);
		if ((destination.split("/").length > 2)
				&& (!isExistFilePath(destination.substring(0,
						destination.lastIndexOf('/'))))) {
			buildList(destination.substring(0, destination.lastIndexOf('/')));
		}

		InputStream ftpIn = new ByteArrayInputStream(sourceData);
		try {
			if (!this.ftpclient.storeFile(destination, ftpIn)) {
				ftpIn.close();
				throw new Exception("upFile fail!" + destination);
			}
		} finally {
			IOUtils.closeQuietly(ftpIn);
		}
	}

	public void downFile(String SourceFileName, String destinationFileName)
			throws Exception {
		SourceFileName = checkName(SourceFileName);
		FileOutputStream byteOut = null;
		try {
			byteOut = new FileOutputStream(destinationFileName);
			this.ftpclient.retrieveFile(SourceFileName, byteOut);
		} finally {
			IOUtils.closeQuietly(byteOut);
		}
	}

	public String getMD5andDown(String SourceFileName,
			String destinationFileName) throws Exception {
		String md5 = "";
		InputStream in = null;
		FileOutputStream out = null;
		try {
			in = this.ftpclient.retrieveFileStream(SourceFileName);
			MessageDigest md = MessageDigest.getInstance("MD5");
			Long bufferSize = Long.valueOf(4096L);
			byte[] buffer = new byte[bufferSize.intValue()];
			File file = new File(destinationFileName);
			File parentFile = file.getParentFile();
			if (!parentFile.exists()) {
				parentFile.mkdirs();
			}
			out = new FileOutputStream(destinationFileName);

			this.ftpclient.setBufferSize(bufferSize.intValue());
			int numRead = 0;
			while ((numRead = in.read(buffer)) > 0) {
				md.update(buffer, 0, numRead);
				out.write(buffer, 0, numRead);
			}
			out.close();
			in.close();

			byte[] b = md.digest();

			StringBuffer buf = new StringBuffer("");
			for (int offset = 0; offset < b.length; offset++) {
				int i = b[offset];
				if (i < 0)
					i += 256;
				if (i < 16)
					buf.append("0");
				buf.append(Integer.toHexString(i));
			}
			this.ftpclient.completePendingCommand();
			md5 = buf.toString();
		} finally {
			IOUtils.closeQuietly(in);
			IOUtils.closeQuietly(out);
		}
		return md5;
	}

	public boolean mkdir(String pathName) throws IOException{
		return this.ftpclient.makeDirectory(pathName);
	}

	public byte[] downFile(String SourceFileName) throws IOException {
		SourceFileName = checkName(SourceFileName);
		ByteArrayOutputStream byteOut = null;
		try {
			byteOut = new ByteArrayOutputStream();
			this.ftpclient.retrieveFile(SourceFileName, byteOut);
		} finally {
			IOUtils.closeQuietly(byteOut);
		}
		return byteOut.toByteArray();
	}

	public FTPFile[] getFiles() throws Exception {
		FTPFile[] ftpFiles = this.ftpclient.listFiles();
		return ftpFiles;
	}

	public boolean deleteFile(String filePath) throws IOException {
		return this.ftpclient.deleteFile(filePath);
	}

	private String checkName(String name) {
		if (name.startsWith("/")) {
			name = name.substring(1);
		}
		return name;
	}

	public boolean changePath(String path) throws IOException {
		path = path.startsWith("/") ? path.substring(1) : path;
		path = path.endsWith("/") ? path.substring(0, path.length() - 1) : path;
		return this.ftpclient.changeWorkingDirectory(path);
	}

	public boolean changeParentPath() throws IOException {
		return this.ftpclient.changeToParentDirectory();
	}
	
	public static String getFtpFile(String url) throws Exception{
		String charsets ="UTF-8";
		String[] values = getSingleMatchValue(url);
		FtpUtil ftpUtil = new FtpUtil(values[2],Integer.parseInt(values[3].toString()), values[0], values[1]);
		
		byte[] bytes;
		try {
			ftpUtil.login();
			bytes = ftpUtil.downFile("/" + values[4]);
		} finally {
			if(ftpUtil != null) {
				try {
					ftpUtil.logout();
				} catch (Exception e) {
				}
			}
		}
		
		return new String(bytes, charsets);
	}
	
	public static String[] getSingleMatchValue(String str) {
		String[] values = (String[]) null;
		if (str == null) {
			return values;
		}
		String[] strs = str.split("://");
		if ((strs == null) || (strs.length == 0)) {
			return values;
		}
		if ((strs == null) || (strs.length < 2)
				|| (!strs[0].equalsIgnoreCase("ftp"))) {
			return values;
		}
		values = new String[5];
		String address = strs[1].replaceAll("//", "/");
		values[0] = address.substring(0, address.indexOf(58));
		address = address.substring(address.indexOf(':'), address.length());
		values[1] = address.substring(1, address.indexOf(64));
		address = address.substring(address.indexOf('@'), address.length());
		values[2] = address.substring(1, address.indexOf(47));
		if (values[2].contains(":")) {
			String[] temp = values[2].split(":");
			values[2] = temp[0];
			values[3] = temp[1];
		} else {
			values[3] = "21";
		}
		address = address.substring(address.indexOf('/'), address.length());
		values[4] = address.substring(1, address.length());

		return values;
	}

	public String getFilePath() {
		return this.filePath;
	}

	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}

	/**
	 * @param localpath     本地路径
	 * @param orgFTPFileUrl FTP路径
	 * @return flag=true: 下载成功     flag=false: 下载失败
	 */
	public static boolean downloadFileFormFtp(String localpath, String orgFTPFileUrl) throws IOException {
		boolean flag;
		FTPClient ftpClient = new FTPClient();
		String[] values = getSingleMatchValue(orgFTPFileUrl);
		ftpClient.connect(values[2], Utils.getInt(values[3],21));
		ftpClient.login(values[0], values[1]);
		File file = new File(localpath);
		if (!file.getParentFile().exists()) {
			file.getParentFile().mkdirs();
		}
		try (FileOutputStream fos= new FileOutputStream(localpath)){
			ftpClient.setBufferSize(1024);
			//设置文件类型（二进制）
			ftpClient.setFileType(FTPClient.BINARY_FILE_TYPE);
			ftpClient.enterLocalPassiveMode();
			ftpClient.retrieveFile("/" + values[4], fos);
			//退出ftp
			ftpClient.logout();
			flag = true;
			ftpClient.disconnect();
			log.info("====下载成功====");
		} catch (Exception e) {
			flag = false;
			log.error("" + e.getMessage(), e);
		}
		return flag;
	}

	public static void main(String[]a) throws Exception{
		String xmlurl="ftp://cms1:sdYD_qd#qwe@10.2.1.176//XmlRspPath/test.xml";
		String contentmap=FtpUtil.getFtpFile(xmlurl);
		System.out.println(contentmap);
	}
}
