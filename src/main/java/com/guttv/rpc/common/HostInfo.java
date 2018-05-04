/**
 * 
 */
package com.guttv.rpc.common;

import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;

/**
 * @author Peter
 *
 */
public class HostInfo {

	static {
		String localIP = "127.0.0.1";

		String ip172 = null;

		String tmp = null;
		Enumeration<?> e1;
		try {
			e1 = NetworkInterface.getNetworkInterfaces();
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

			// 最后才选172的IP
			if ("127.0.0.1".equals(localIP) && ip172 != null) {
				localIP = ip172;
			}
			hostAddr = localIP;

		} catch (Exception e) {
			e.printStackTrace();
			hostAddr = "0.0.0.0";
		}

		try {
			hostName = InetAddress.getLocalHost().getHostName();
		} catch (Exception e) {
			e.printStackTrace();
			hostName = "Unknow";
		}
	}

	private static String hostAddr;
	private static String hostName;

	public static String getHostAddr() {
		return hostAddr;
	}

	public static String getHostName() {
		return hostName;
	}
}
