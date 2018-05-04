/**
 * 
 */
package com.guttv.pm.utils;

import com.guttv.pm.core.cache.ConfigCache;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.ClassUtils;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

/**
 * @author Peter
 *
 */
public class Utils {

	/**
	 * 
	 * @return
	 */
	public static String getProjectLocation() {
		String location = System
				.getenv(ConfigCache.getInstance().getProperty(Constants.SERVER_LOCATION_ENV_KEY, Constants.PM_HOME));
		if (StringUtils.isBlank(location)) {
			location = ClassUtils.getDefaultClassLoader().getResource("").getPath();
		}
		return location;
	}

	/**
	 * 获取配置文件的路径
	 * 
	 * @return
	 */
	public static File getPropertiesFile() {
		String location = getProjectLocation();
		File file = new File(location + File.separator + "config" + File.separator + "application.properties");
		if (file.exists()) {
			return file;
		} else {
			file = new File(location + File.separator + "application.properties");
		}
		return file.exists() ? file : null;
	}

	public static final String DEFAULT_TIME_FORMAT1 = "yyyy-MM-dd HH:mm:ss";
	public static final String TIME_FORMAT2 = "yyyyMMddHHmmss";

	/**
	 * 用的默认时间类型 DEFAULT_TIME_FORMAT1
	 * 
	 * @param timeString
	 * @return
	 * @throws ParseException
	 */
	public static Date getTime(String timeString) throws ParseException {
		return getTime(timeString, DEFAULT_TIME_FORMAT1);
	}

	public static Date getTime(String timeString, String format) throws ParseException {
		SimpleDateFormat s = new SimpleDateFormat(format);
		return s.parse(timeString);
	}

	/**
	 * 获取日期的字符串
	 * 
	 * @param Millis
	 * @param format
	 * @return
	 */
	public static String getTimeString(long Millis, String format) {
		SimpleDateFormat s = new SimpleDateFormat(format);
		return s.format(new Date(Millis));
	}

	public static String getTimeString(long Millis) {
		return getTimeString(Millis, DEFAULT_TIME_FORMAT1);
	}

	/**
	 * 
	 * @param format
	 * @return
	 */
	public static String getCurrentTimeString(String format) {
		return getTimeString(System.currentTimeMillis(), format);
	}

	/**
	 * 默认时间格式 DEFAULT_TIME_FORMAT1 yyyyMMddHHmmss
	 * 
	 * @return
	 */
	public static String getCurrentTimeString() {
		return getCurrentTimeString(DEFAULT_TIME_FORMAT1);
	}

	/**
	 * 获取当前内存信息
	 * 
	 * @return
	 */
	public static String getMemoryInfo() {
		long maxMem = Runtime.getRuntime().maxMemory() / 1024 / 1024;
		long freeMem = Runtime.getRuntime().freeMemory() / 1024 / 1024;
		StringBuilder sb = new StringBuilder();
		long usedMem = maxMem - freeMem;
		sb.append("MaxMemory:").append(maxMem).append("MB,");
		sb.append("UsedMemory:").append(usedMem).append("MB,");
		sb.append("FreeMemory:").append(freeMem).append("MB");
		return sb.toString();
	}

	public static Thread[] getAllThread() {
		ThreadGroup group = Thread.currentThread().getThreadGroup();
		ThreadGroup topGroup = group;
		// 遍历线程组树，获取根线程组
		while (group != null) {
			topGroup = group;
			group = group.getParent();
		}
		// 激活的线程数加倍
		int estimatedSize = topGroup.activeCount() * 2;
		Thread[] slackList = new Thread[estimatedSize];

		// 获取根线程组的所有线程
		int actualSize = topGroup.enumerate(slackList);
		// copy into a list that is the exact size
		Thread[] list = new Thread[actualSize];
		System.arraycopy(slackList, 0, list, 0, actualSize);
		return list;
	}

	public static int[] getPortScope(String str) {
		int[] scope = new int[] { -1, -1 };
		if (StringUtils.isNotBlank(str)) {
			String[] scopeArray = str.split(",");
			if (scopeArray.length == 2) {
				scope[0] = Integer.parseInt(scopeArray[0].trim());
				scope[1] = Integer.parseInt(scopeArray[1].trim());
			}
		}
		return scope;
	}

	/**
	 * 获取规范的时间长度
	 * 
	 * @param time
	 * @return
	 */
	public static String getBeautifulTimeLengh(long time) {
		StringBuilder sb = new StringBuilder();

		long second = time % 1000;
		sb.append(second).append("毫秒");

		time = time / 1000;
		if (time > 0) {
			long min = time % 60;
			sb.insert(0, min + "秒");
		}

		time = time / 60;
		if (time > 0) {
			long hour = time % 60;
			sb.insert(0, hour + "分");
		}

		time = time / 60;
		if (time > 0) {
			long day = time % 24;
			sb.insert(0, day + "时");
		}
		time = time / 24;
		if (time > 0) {
			sb.insert(0, time + "天");
		}

		return sb.toString();
	}

	/**
	 * 把文件类型的路径转成类名，/转成.,并去掉后面的.class
	 * 
	 * @param resource
	 * @return
	 */
	public static String resourceAsClassName(String resource) {
		return resource.replaceAll("/", ".").substring(0, resource.length() - 6);
	}




	/**
	 * 获取uuid
	 *
	 * @return
	 */
	public static String getUUID() {
		UUID uuid = UUID.randomUUID();
		String tmpid = uuid.toString();
		tmpid = tmpid.replace("-", "");
		return tmpid;
	}

	public static int getInt(Object obj, int defvalue) {
		int tmpret = defvalue;
		if (obj != null) {
			try {
				tmpret = Integer.parseInt(obj.toString());
			} catch (Exception ex) {

			}
		}
		return tmpret;
	}

	public static String getString(Object obj) {
		String tmpret = "";
		if (obj != null){
			tmpret = obj.toString();
		}
		return tmpret.trim();
	}

	public static Long getLong(Object obj) {
		if (obj == null){
			return null;
		}
		if (isDia(obj)) {
			Long tmpret = Long.valueOf(obj.toString().trim());
			return tmpret;
		}
		return null;
	}

	public static Long getLong(Object obj, Long defvalue) {
		Long tmpret = defvalue;
		if (isDia(obj)) {
			tmpret = Long.valueOf(obj.toString().trim());
			return tmpret;
		}
		return tmpret;
	}

	public static boolean isDia(Object obj) {
		if (obj == null || ("" + obj).trim().equals("")) {
			return false;
		}
		String s = ("" + obj).trim();
		s = s.replaceAll("-", "");
		char[] c = s.toCharArray();
		for (int i = 0; i < c.length; i++) {
			if (c[i] > '9' || c[i] < '0') {
				return false;
			}
		}
		return true;
	}
}
