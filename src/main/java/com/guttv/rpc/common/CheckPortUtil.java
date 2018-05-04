/**
 * 
 */
package com.guttv.rpc.common;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;

/**
 * @author Peter
 *
 */
public class CheckPortUtil {

	/**
	 * 检查端口是否可用
	 * 
	 * @param port
	 * @return
	 */
	public static boolean isAvailable(int port) {
		ServerSocket sskt = null;
		try {
			sskt = new ServerSocket(port);
		} catch (Exception e) {
			return false;
		} finally {
			if (sskt != null) {
				try {
					sskt.close();
				} catch (IOException e) {
				}
			}
		}
		return true;
	}

	/**
	 * 当本地IP有多个的情况下，启动socket服务时绑定IP,port时用可以用该方法检测
	 * 
	 * 假如：服务器的IP有：127.0.0.1 和 10.2.1.218 程序在启动服务时 绑定的是 127.0.0.1:9009 本方法
	 * isAvailable("10.2.1.218",9009)返回 true 即可用 调用 isAvailable(9009)返回 false
	 * 
	 * 由上面可以遇见 isAvailable(port) 方法用的场合比较多
	 * 
	 * @param host
	 * @param port
	 * @return
	 */
	public static boolean isAvailable(String host, int port) {

		ServerSocket sskt = null;
		try {
			sskt = new ServerSocket(port, 50, InetAddress.getByName(host));
		} catch (IOException e) {
			return false;
		} finally {
			if (sskt != null) {
				try {
					sskt.close();
				} catch (IOException e) {
				}
			}
		}
		return true;

	}

	/**
	 * 判断主机是否可达
	 * 
	 * @param host
	 * @param timeout
	 *            the time, in milliseconds
	 * @return
	 */
	public static boolean isReachable(String host, int timeout) {
		try {
			if(!Pinger.isReachable(host, 1, timeout)) {
				return InetAddress.getByName(host).isReachable(timeout);
			}
			return true;
		} catch (Throwable e) {
		}
		return false;
	}
	
	public static void main(String[]a) {
		System.out.println(System.currentTimeMillis());
		System.out.println(isReachable("127.0.0.1",10000));
		System.out.println(System.currentTimeMillis());
	}
}
