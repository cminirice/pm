package com.guttv.rpc.common;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;

public class Pinger {

	/**
	 * 测试是否能ping通
	 * 
	 * @param remoteIpAddress
	 * @param pingTimes
	 * @param timeOut
	 * @return
	 */
	public static boolean isReachable(String remoteIpAddress, int pingTimes, int timeOut) {
		BufferedReader in = null;
		Runtime r = Runtime.getRuntime();
		// 将要执行的ping命令
		String pingCommand = null;
		if (isWindows()) {
			// 此命令是windows格式的命令
			pingCommand = "ping " + remoteIpAddress + " -n " + pingTimes + " -w " + timeOut;
		} else {
			// Linux命令 w后面的超时时间默认为10S，如果小于等于0的话，用默认时间，单位是：秒
			pingCommand = "ping " + remoteIpAddress + " -c " + pingTimes + " -w " + (timeOut / 1000);
		}

		try {
			// 执行命令并获取输出
			// System.out.println(pingCommand);
			Process p = r.exec(pingCommand);
			if (p == null) {
				return false;
			}
			in = new BufferedReader(new InputStreamReader(p.getInputStream(), "UTF-8"));
			// 逐行检查输出,计算类似出现=23ms TTL=62字样的次数
			int connectedCount = 0;
			String line = null;
			while ((line = in.readLine()) != null) {
				connectedCount += getCheckResult(line);
				if (connectedCount == pingTimes) {
					return true;
				}
			}
			// 如果出现类似=23ms TTL=62这样的字样,出现的次数=测试次数则返回真
			return connectedCount == pingTimes;
		} catch (Exception ex) {
			return false;
		} finally {
			IOUtils.closeQuietly(in);
		}
	}

	public static boolean isWindows() {
		String os = System.getProperty("os.name");
		return os.toLowerCase().indexOf("win") >= 0;
	}

	/**
	 * 若line含有=18ms TTL=16字样,说明已经ping通,返回1,否則返回0.
	 * 
	 * @param line
	 * @return
	 */
	private static int getCheckResult(String line) {
		// System.out.println("控制台输出的结果为:" + line);
		Pattern pattern = Pattern.compile("(\\d+ms)(\\s+)(TTL=\\d+)", Pattern.CASE_INSENSITIVE);
		Matcher matcher = pattern.matcher(line);
		while (matcher.find()) {
			return 1;
		}
		return 0;
	}

	public static void main(String[] args) {
		System.out.println(System.currentTimeMillis());
		System.out.println(Pinger.isReachable("10.4.1.1", 1, 5000));
		System.out.println(System.currentTimeMillis());
	}
}
