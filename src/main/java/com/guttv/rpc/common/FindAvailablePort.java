/**
 * 
 */
package com.guttv.rpc.common;

/**
 * @author Peter
 *
 */
public class FindAvailablePort {
	
	public static void main(String[]a) {
		System.out.println(CheckPortUtil.isAvailable(-1));
	}

	/**
	 * 如果没有可用的端口返回 -1， 本方法没有校验区间的有效性 两边都是闭区间
	 * 
	 * @param from
	 * @param to
	 * @return
	 */
	public static int find(int from, int to) {

		int port = -1;
		for (int i = from; i <= to; i++) {
			if (CheckPortUtil.isAvailable(i)) {
				port = i;
				break;
			}
		}
		return port;
	}

	public static int find(int from, int to, int def) {
		int port = find(from, to);
		return port < 0 ? (CheckPortUtil.isAvailable(def) ? def : -1) : port;
	}
}
