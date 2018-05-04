/**
 * 
 */
package com.guttv.pm.utils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.objenesis.Objenesis;
import org.objenesis.ObjenesisStd;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.guttv.pm.core.flow.FlowExecuteEngine;

/**
 * @author Peter
 *
 */
public class ReflectUtil {

	private static Logger logger = LoggerFactory.getLogger(FlowExecuteEngine.class);

	/**
	 * 获取对象中某个字段的值
	 * 
	 * @param obj
	 * @param fieldName
	 * @return
	 * @throws Exception
	 */
	public static Object getFieldValue(Object obj, String fieldName) throws Exception {
		Class<?> clz = obj.getClass();
		Field field = getDeclaredField(clz, fieldName);
		if (field == null) {
			throw new Exception("类[" + clz.getName() + "]中及其父类没有属性[" + fieldName + "]");
		}
		boolean access = field.isAccessible();
		field.setAccessible(true);
		Object value = field.get(obj);
		field.setAccessible(access);
		return value;
	}

	/**
	 * 向某个对象的属性赋值
	 * 
	 * @param obj
	 * @param fieldName
	 * @param value
	 * @throws Exception
	 */
	public static void evaluate2Field(Object obj, String fieldName, Object value) throws Exception {
		Class<?> clz = obj.getClass();
		Field field = getDeclaredField(clz, fieldName);
		if (field == null) {
			throw new Exception("类[" + clz.getName() + "]中及其父类没有属性[" + fieldName + "]");
		}
		evaluate2FieldQuiet(obj, field, value);
	}

	/**
	 * 获取所有属性，包括父类
	 * @param clz
	 * @return
	 */
	public static Field[] getDeclaredFields(Class<?> clz) {
		List<Field> fields = new ArrayList<Field>();
		for (; clz != Object.class; clz = clz.getSuperclass()) {
			fields.addAll(Arrays.asList(clz.getDeclaredFields()));
		}
		return fields.toArray(new Field[fields.size()]);
	}
	
	/**
	 * 获取字段
	 * 
	 * @param clz
	 * @param fieldName
	 * @return
	 */
	private static Field getDeclaredField(Class<?> clz, String fieldName) {
		Field field = null;

		for (; clz != Object.class; clz = clz.getSuperclass()) {
			try {
				field = clz.getDeclaredField(fieldName);
				return field;
			} catch (Exception e) {
			}
		}
		return null;
	}

	/**
	 * 向某个对象的属性赋值
	 * 
	 * @param obj
	 * @param fieldName
	 * @param value
	 * @return 赋值的结果
	 */
	public static boolean evaluate2FieldQuiet(Object obj, String fieldName, Object value) {
		try {
			Class<?> clz = obj.getClass();
			Field field = getDeclaredField(clz, fieldName);
			if (field == null) {
				return false;
			}
			evaluate2FieldQuiet(obj, field, value);
			return true;
		} catch (Throwable e) {
			return false;
		}
	}

	public static void evaluate2FieldQuiet(Object obj, Field field, Object value) throws Exception {
		// 取出第一个参数值
		Class<?> type = field.getType();
		boolean access = field.isAccessible();
		field.setAccessible(true);

		try {
			if(value == null) {
				field.set(obj, null);
			}else if(type.equals(value.getClass())) {
				field.set(obj, value);
			}else {
				if ("int".equals(type.getName()) || Integer.class.equals(type)) {
					field.set(obj, new Integer(value.toString().trim()));
				} else if ("float".equals(type.getName()) || Float.class.equals(type)) {
					field.set(obj, new Float(value.toString().trim()));
				} else if ("long".equals(type.getName()) || Long.class.equals(type)) {
					field.set(obj, new Long(value.toString().trim()));
				} else if ("short".equals(type.getName()) || Short.class.equals(type)) {
					field.set(obj, new Short(value.toString().trim()));
				} else if ("double".equals(type.getName()) || Double.class.equals(type)) {
					field.set(obj, new Double(value.toString().trim()));
				} else if ("byte".equals(type.getName()) || Byte.class.equals(type)) {
					field.set(obj, new Byte(value.toString().trim()));
				} else if ("boolean".equals(type.getName()) || Boolean.class.equals(type)) {
					field.set(obj, new Boolean(value.toString().trim()));
				} else if ("char".equals(type.getName())) {
					field.set(obj, value.toString().trim().charAt(0));
				} else if ("java.util.Date".equals(type.getName())) {
					SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmss");
					field.set(obj, format.parse(value.toString().trim()));
				} else {
					field.set(obj, value.toString().trim());
				}
			}
		} finally {
			field.setAccessible(access);
		}
	}

	/**
	 * 
	 * @param obj
	 * @param fieldName
	 * @param value
	 * @throws Exception
	 */
	public static void invokeSetMethod(Object obj, String fieldName, Object value) throws Exception {

		// 查找set方法
		String methodName = "set" + StringUtils.capitalize(fieldName);
		Method[] ms = obj.getClass().getMethods();
		Method m = null;
		if (ms != null && ms.length > 0) {
			for (Method me : ms) {
				if (me.getName().equals(methodName)) {
					m = me;
					break;
				}
			}
		}
		if (m == null) {
			String error = "类[" + obj.getClass().getName() + "]中没有属性[" + fieldName + "]";
			logger.warn(error);
			throw new Exception(error);
		}

		// 取出第一个参数值
		Class<?> type = m.getParameterTypes()[0];

		if ("int".equals(type.getName()) || "java.lang.Integer".equals(type.getName())) {
			m.invoke(obj, value == null ? null : new Integer(value.toString().trim()));
		} else if ("float".equals(type.getName()) || "java.lang.Float".equals(type.getName())) {
			m.invoke(obj, value == null ? null : new Float(value.toString().trim()));
		} else if ("long".equals(type.getName()) || "java.lang.Long".equals(type.getName())) {
			m.invoke(obj, value == null ? null : new Long(value.toString().trim()));
		} else if ("short".equals(type.getName()) || "java.lang.Short".equals(type.getName())) {
			m.invoke(obj, value == null ? null : new Short(value.toString().trim()));
		} else if ("double".equals(type.getName()) || "java.lang.Double".equals(type.getName())) {
			m.invoke(obj, value == null ? null : new Double(value.toString().trim()));
		} else if ("byte".equals(type.getName()) || "java.lang.Byte".equals(type.getName())) {
			m.invoke(obj, value == null ? null : new Byte(value.toString().trim()));
		} else if ("boolean".equals(type.getName()) || "java.lang.Boolean".equals(type.getName())) {
			m.invoke(obj, new Boolean(value == null ? null : value.toString().trim()));
		} else if ("char".equals(type.getName())) {
			m.invoke(obj, value == null ? null : value.toString().trim().charAt(0));
		} else if ("java.util.Date".equals(type.getName())) {
			SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmss");
			m.invoke(obj, value == null ? null : format.parse(value.toString().trim()));
		} else {
			m.invoke(obj, value == null ? null : value.toString().trim());
		}
	}

	private static Objenesis objenesis = new ObjenesisStd(true);
	
	private static Objenesis objenesisWithNoCache = new ObjenesisStd(false);

	/**
	 * 获取对象的实例，该方法可以避开 私有构造函数，有参构造函数，让单例不再单例
	 * 
	 * @param clz
	 * @return
	 */
	public static <T> T newInstance(Class<T> clz) {
		return objenesis.newInstance(clz);
	}
	
	public static <T> T newInstance(Class<T> clz,boolean useCache) {
		if(useCache) {
			return objenesis.newInstance(clz);
		}else {
			return objenesisWithNoCache.newInstance(clz);
		}
	}
}
