/**
 * 
 */
package com.guttv.pm.util;

import com.guttv.pm.utils.HttpUtil;

/**
 * @author Peter
 *
 */
public class HttpUtilTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception{
		//System.out.println(HttpUtil.getMac());
		//System.out.println(HttpUtil.getLocalIP());

		String url = "http://10.4.1.2:9090/pm/rest/notice?contentType=2&actionType=1&contentCode=qwersssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssstyuioplkjhgfdsa";
		for (int i = 0; i < 50000; i++) {
			HttpUtil.httpRequest(url, "GET", "application/text;charset=UTF-8", "UTF-8", null, 5000, 5000);
		}
	}

}
