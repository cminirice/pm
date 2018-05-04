/**
 * 
 */
package com.guttv.pm.utils;

import com.google.gson.Gson;

/**
 * @author Peter
 *
 */
public class JsonUtil {

	/**
	 * 
	 * @param obj
	 * @return
	 */
	public static String toJson(Object obj) {
		if (obj == null) {
			return null;
		}
		Gson gson = new Gson();
		return gson.toJson(obj);
	}

	/**
	 * 
	 * @param json
	 * @param clz
	 * @return
	 */
	public static <T> T fromJson(String json, Class<T> clz) {
		Gson gson = new Gson();
		T obj = gson.fromJson(json, clz);
		return obj;
	}
}
