package com.guttv.pm.utils;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author Peter
 *
 */
public class HttpUtil {

	private static Logger log = LoggerFactory.getLogger(HttpUtil.class);

	/**
	 * 
	 * @param url
	 * @param destPath
	 * @param retryNum
	 * @param connectTimeout
	 * @param readTimeout
	 * @throws Exception
	 */
	public static String httpRequestForRetry(String requestUrl, String requestMethod, String contentType,
			String charset, String outputStr) throws Exception {
		return httpRequestForRetry(requestUrl, requestMethod, contentType, charset, outputStr, 5, 30000, 30000);
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
	public static String httpRequestForRetry(String requestUrl, String requestMethod, String contentType,
			String charset, String outputStr, int retryNum, int connectTimeout, int readTimeout) throws Exception {
		String ret = "";

		if (retryNum <= 0) {
			retryNum = 1;
		}

		// 如果没有成功，重试几次
		boolean success = false;
		Exception ex = null;
		for (int i = 0; i < retryNum && !success; i++) {
			try {
				ret = HttpUtil.httpRequest(requestUrl, requestMethod, contentType, charset, outputStr, connectTimeout,
						readTimeout);
				success = true;
			} catch (Exception e) {
				ex = e;
			}
		}

		// 如果没有成功，抛出异常
		if (!success && ex != null) {
			throw new Exception("重试[" + retryNum + "]次后失败", ex);
		}
		return ret;
	}

	/**
	 * 
	 * @param requestUrl
	 *            请求地址
	 * @param requestMethod
	 *            请求方法 POST or GET
	 * @param contentType
	 * @param charset
	 *            字符集
	 * @param outputStr
	 * @return
	 */
	public static String httpRequest(String requestUrl, String requestMethod, String contentType, String charset,
			String outputStr) throws Exception {
		return HttpUtil.httpRequest(requestUrl, requestMethod, contentType, charset, outputStr, 30000, 30000);
	}

	/**
	 * 
	 * @param requestUrl
	 *            请求地址
	 * @param requestMethod
	 *            请求方法 POST or GET
	 * @param contentType
	 * @param charset
	 *            字符集
	 * @param outputStr
	 * @return
	 */
	public static String httpRequest(String requestUrl, String requestMethod, String contentType, String charset,
			String outputStr, int connectTimeout, int readTimeout) throws Exception {
		InputStream inputStream = null;
		InputStreamReader inputStreamReader = null;
		BufferedReader bufferedReader = null;
		HttpURLConnection conn = null;
		try {
			// 创建SSLContext对象，并使用我们指定的信任管理器初始化
			URL url = new URL(requestUrl);
			conn = (HttpURLConnection) url.openConnection();
			conn.setConnectTimeout(connectTimeout);
			conn.setReadTimeout(readTimeout);
			conn.setDoOutput(true);
			conn.setDoInput(true);
			conn.setUseCaches(false);
			// 设置请求方式（GET/POST）
			conn.setRequestMethod(requestMethod);
			conn.setRequestProperty("content-type", contentType);
			// 当outputStr不为null时向输出流写数据
			if (null != outputStr && outputStr.trim().length() > 0 && "POST".equalsIgnoreCase(requestMethod)) {
				OutputStream outputStream = null;
				try {
					outputStream = conn.getOutputStream();
					// 注意编码格式
					outputStream.write(outputStr.getBytes(charset));
				} finally {
					IOUtils.closeQuietly(outputStream);

				}
			}
			// 从输入流读取返回内容

			inputStream = conn.getInputStream();
			inputStreamReader = new InputStreamReader(inputStream);
			bufferedReader = new BufferedReader(inputStreamReader);
			String str = null;
			StringBuffer buffer = new StringBuffer();
			while ((str = bufferedReader.readLine()) != null) {
				buffer.append(new String(str.getBytes(charset)));
			}

			return buffer.toString();
		} finally {
			// 释放资源
			IOUtils.closeQuietly(bufferedReader);
			IOUtils.closeQuietly(inputStreamReader);
			IOUtils.closeQuietly(inputStream);
			try {
				conn.disconnect();
			} catch (Exception e) {
			}
		}
	}

	public static String urlEncodeUTF8(String source) {
		String result = source;
		try {
			result = java.net.URLEncoder.encode(source, "utf-8");
		} catch (UnsupportedEncodingException e) {
		}
		return result;
	}

	/**
	 * 把HTTP请求的参数设置到MAP中
	 * 
	 * @param str
	 * @return
	 */
	public static Map<String, String> parameter2Map(String str) {
		Map<String, String> map = new HashMap<String, String>();
		if (StringUtils.isBlank(str)) {
			return map;
		}

		String[] array = StringUtils.split(str, "&");
		if (array != null && array.length > 0) {
			String[] a = null;
			for (String p : array) {
				a = StringUtils.split(p, "=");
				if (a != null && a.length == 2) {
					map.put(a[0], a[1]);
				}
			}
		}

		return map;
	}

	/**
	 * 把map值，拼成URL的参数
	 * 
	 * @param map
	 * @return
	 */
	public static String map2Parameter(Map<String, String> map) {
		Set<Map.Entry<String, String>> es = map.entrySet();
		Iterator<Map.Entry<String, String>> it = es.iterator();
		String k = null;
		String v = null;
		StringBuilder sb = new StringBuilder();
		while (it.hasNext()) {
			Map.Entry<String, String> entry = it.next();
			k = entry.getKey();
			v = entry.getValue();
			sb.append(k).append("=").append(v).append("&");
		}
		return sb.substring(0, sb.length() - 1);

	}
	
	private static String hostName = null;
	public static String getHostName() throws UnknownHostException {
		if(hostName != null) {
			return hostName;
		}
		hostName = InetAddress.getLocalHost().getHostName();
		return hostName;
	}
	
	private static String localIP = null;
	public static String getLocalIP() {
		if (localIP != null) {
			return localIP;
		}

		localIP = "127.0.0.1";

		String ip172 = null;
		try {
			String tmp = null;
			Enumeration<?> e1 = NetworkInterface.getNetworkInterfaces();
			while (e1.hasMoreElements()) {
				NetworkInterface ni = null;
				try {
					ni = (NetworkInterface) e1.nextElement();
				} catch (Exception ex) {
					continue;
				}

				Enumeration<?> e2 = ni.getInetAddresses();
				while (e2.hasMoreElements()) {
					InetAddress ia = null;
					try {
						ia = (InetAddress) e2.nextElement();
					} catch (Exception ex) {
						continue;
					}

					if (ia instanceof Inet6Address) {
						continue;
					}
					tmp = ia.getHostAddress();
				}

				if (tmp == null || "127.0.0.1".equals(tmp)) {
					continue;
				} else {
					if (tmp != null) {
						if (tmp.startsWith("172")) {
							ip172 = tmp;
							continue;
						}
						localIP = tmp;
						break;
					}
				}
			}
		} catch (SocketException e) {
			log.error(e.getMessage(), e);
		}

		// 最后才选172的IP
		if ("127.0.0.1".equals(localIP) && ip172 != null) {
			localIP = ip172;
		}

		return localIP;
	}

	private static String ipList = null;

	public static String getIPList() {
		if (ipList != null) {
			return ipList;
		}

		String ip = "127.0.0.1";

		Set<String> ipSet = new HashSet<String>();
		String ip172 = null;
		try {
			String tmp = null;
			Enumeration<?> e1 = NetworkInterface.getNetworkInterfaces();
			while (e1.hasMoreElements()) {
				NetworkInterface ni = null;
				try {
					ni = (NetworkInterface) e1.nextElement();
				} catch (Exception ex) {
					continue;
				}

				Enumeration<?> e2 = ni.getInetAddresses();
				while (e2.hasMoreElements()) {
					InetAddress ia = null;
					try {
						ia = (InetAddress) e2.nextElement();
					} catch (Exception ex) {
						continue;
					}

					if (ia instanceof Inet6Address) {
						continue;
					}
					tmp = ia.getHostAddress();
				}

				if (tmp == null || "127.0.0.1".equals(tmp)) {
					continue;
				} else {
					if (tmp != null) {
						if (tmp.startsWith("172")) {
							ip172 = tmp;
							continue;
						}
						ipSet.add(tmp);
						if (StringUtils.isBlank(localIP)) {
							localIP = tmp;
						}
					}
				}
			}
		} catch (SocketException e) {
			log.error(e.getMessage(), e);
		}

		// 最后才选172的IP
		if ("127.0.0.1".equals(ip) && ip172 != null) {
			ip = ip172;
			ipSet.add(ip172);
		}

		ipSet.add("127.0.0.1");
		StringBuilder sb = new StringBuilder();
		for (String s : ipSet) {
			if (sb.length() > 0) {
				sb.append(",");
			}
			sb.append(s);
		}
		ipList = sb.toString();
		return ipList;
	}

	private static String mac = null;

	/**
	 * 获取本机某个IP对应的mac
	 * 
	 * @return
	 */
	public static String getMac() {
		if (mac != null) {
			return mac;
		}

		byte[] macbyte = null;
		String ip = getLocalIP();
		try {
			macbyte = NetworkInterface.getByInetAddress(InetAddress.getByName(ip)).getHardwareAddress();
		} catch (Exception e) {
			log.error("获取本地mac时异常：" + e.getMessage(), e);
			return null;
		}

		StringBuffer sb = new StringBuffer("");
		for (int i = 0; i < macbyte.length; i++) {
			if (i != 0) {
				sb.append("-");
			}
			// 字节转换为整数
			int temp = macbyte[i] & 0xff;
			String str = Integer.toHexString(temp);
			if (str.length() == 1) {
				sb.append("0" + str);
			} else {
				sb.append(str);
			}
		}

		mac = sb.toString().toUpperCase();

		log.debug("网卡[" + ip + "]对应的mac是[" + mac + "]");

		return mac;
	}

	/**
	 * Do GET request
	 * 
	 * @param url
	 * @return
	 * @throws Exception
	 */
	public static String doGet(String url) throws Exception {
		String response = null;
		boolean retry = true;
		Exception ex = null;
		for (int i = 0; i < 3 && retry; i++) {
			URL localUrl = new URL(url);
			HttpURLConnection httpURLConnection = (HttpURLConnection) localUrl.openConnection();
			httpURLConnection.setConnectTimeout(30000);
			httpURLConnection.setReadTimeout(30000);
			if (httpURLConnection.getResponseCode() == 200) {
				InputStream inputStream = null;
				try {
					inputStream = httpURLConnection.getInputStream();
					response = IOUtils.toString(inputStream, Constants.ENCODING);
					retry = false;
				} catch (Exception e) {
					ex = e;
				} finally {
					IOUtils.closeQuietly(inputStream);
					if (httpURLConnection != null) {
						try {
							httpURLConnection.disconnect();
						} catch (Exception e) {
						}
					}
				}
			}
		}

		if (retry && ex != null) {
			throw ex;
		}

		return response;
	}
}