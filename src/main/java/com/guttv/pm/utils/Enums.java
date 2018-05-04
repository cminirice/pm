/**
 * 
 */
package com.guttv.pm.utils;

import java.lang.reflect.Field;
import java.util.List;

import org.apache.commons.lang3.EnumUtils;
import org.apache.commons.lang3.StringUtils;

/**
 * @author Peter
 *
 */
public class Enums {

	public static void main(String[] a) {
		YesOrNo status = getEnumByValue(YesOrNo.class, 1);
		System.out.println(status);
		status = getEnumByName(YesOrNo.class, "是");
		System.out.println(status);

		status = getEnum(YesOrNo.class, "yes");
		System.out.println(status);

		ComponentRunType type = getEnumByValue(ComponentRunType.class, "cycle");
		System.out.println(type);
	}

	public static <T extends Enum<T>> T getEnum(Class<T> enumClass, String name) {
		if (StringUtils.isBlank(name)) {
			return null;
		}
		List<T> enums = EnumUtils.getEnumList(enumClass);
		if (enums == null || enums.size() == 0) {
			return null;
		}
		T ins = null;
		for (T e : enums) {
			if (name.equalsIgnoreCase(e.name())) {
				ins = e;
				break;
			}
		}
		return ins;
	}

	public static <T extends Enum<T>> T getEnumByName(Class<T> enumClass, Object value) {
		if (value == null) {
			return null;
		}

		Field valueField = null;
		try {
			valueField = enumClass.getDeclaredField("name");
		} catch (Exception e1) {
			return null;
		}

		if (valueField == null) {
			return null;
		}

		boolean access = valueField.isAccessible();
		valueField.setAccessible(true);

		List<T> enums = EnumUtils.getEnumList(enumClass);
		if (enums == null || enums.size() == 0) {
			return null;
		}
		T ins = null;
		for (T e : enums) {
			try {
				if (value.equals(valueField.get(e))) {
					ins = e;
					break;
				}
			} catch (Exception e1) {
				break;
			}
		}
		valueField.setAccessible(access);
		return ins;
	}

	public static <T extends Enum<T>> T getEnumByValue(Class<T> enumClass, Object value) {
		if (value == null) {
			return null;
		}

		Field valueField = null;
		try {
			valueField = enumClass.getDeclaredField("value");
		} catch (Exception e1) {
			return null;
		}

		if (valueField == null) {
			return null;
		}

		boolean access = valueField.isAccessible();
		valueField.setAccessible(true);

		List<T> enums = EnumUtils.getEnumList(enumClass);
		if (enums == null || enums.size() == 0) {
			return null;
		}
		T ins = null;
		for (T e : enums) {
			try {
				if (value.equals(valueField.get(e))) {
					ins = e;
					break;
				}
			} catch (Exception e1) {
				break;
			}
		}
		valueField.setAccessible(access);
		return ins;
	}

	/**
	 * 全局是否值
	 * 
	 * @author Peter
	 *
	 */
	public static enum YesOrNo {
		YES(1, "是"), NO(0, "非");
		YesOrNo(int value, String name) {
			this.value = value;
			this.name = name;
		}

		private int value;
		private String name = null;

		public int getValue() {
			return this.value;
		}

		public String getName() {
			return this.name;
		}
	}

	/**
	 * 组件流程的状态
	 * 
	 * @author Peter
	 *
	 */
	public static enum FlowStatus {
		FORBIDDEN(YesOrNo.NO.getValue(), "停用"), NORMAL(YesOrNo.YES.getValue(), "正常"), ILL(2, "异常");
		private FlowStatus(int value, String name) {
			this.value = value;
			this.name = name;
		}

		private int value;
		private String name = null;

		public int getValue() {
			return this.value;
		}

		public String getName() {
			return this.name;
		}
	}

	/**
	 * 
	 * ComponentBean 类中的status字段的取值
	 * 
	 * @author Peter
	 *
	 */
	public static enum ComponentStatus {

		FORBIDDEN(YesOrNo.NO.getValue(), "停用"), NORMAL(YesOrNo.YES.getValue(), "正常");

		private ComponentStatus(int value, String name) {
			this.value = value;
			this.name = name;
		}

		private int value;
		private String name = null;

		public int getValue() {
			return this.value;
		}

		public String getName() {
			return this.name;
		}
	}

	public static enum ComponentRunType {
		Cycle("cycle", "周期"), Once("once", "单次"), Scheduler("scheduler", "调度"), Rest("rest", "Rest接口");
		private ComponentRunType(String value, String name) {
			this.value = value;
			this.name = name;
		}

		private String value;
		private String name = null;

		public String getValue() {
			return this.value;
		}

		public static ComponentRunType get(String value) {
			return getEnumByValue(ComponentRunType.class, value);
		}

		public String getName() {
			return this.name;
		}
	}

	public static enum ComponentProType {
		NOR(1, "普通配置"), DEV(2, "研发配置");
		private ComponentProType(int value, String name) {
			this.value = value;
			this.name = name;
		}

		private int value;
		private String name = null;

		public int getValue() {
			return this.value;
		}

		public String getName() {
			return this.name;
		}
	}

	/**
	 * 
	 * ComponentNodeBean 类中的status字段的取值
	 * 
	 * @author Peter
	 *
	 */
	public static enum ComponentNodeStatus {
		INIT(1, "初始化"), STARTING(2, "启动中"), RUNNING(5, "执行中"), PAUSE(6, "暂停中"), STOPPED(7, "停止"), FINISH(10,
				"执行结束"), ERROR(11, "执行异常"), LOCKED(110, "状态锁定"), FORBIDDEN(111, "禁用");
		private ComponentNodeStatus(int value, String name) {
			this.value = value;
			this.name = name;
		}

		private int value;
		private String name = null;

		public int getValue() {
			return this.value;
		}

		public String getName() {
			return this.name;
		}

		public static ComponentNodeStatus valueOf(int status) {
			return getEnumByValue(ComponentNodeStatus.class, status);
		}
	}

	/**
	 * 流程的状态
	 * 
	 * @author Peter
	 *
	 */
	public static enum FlowExecuteStatus {
		INIT(1, "初始化"), STARTING(2, "启动中"), RUNNING(5, "执行中"), PAUSE(6, "暂停中"), STOPPED(7, "停止"), FINISH(10,
				"执行结束"), ERROR(11, "执行异常"), LOCKED(110, "状态锁定"), FORBIDDEN(111, "禁用");
		private FlowExecuteStatus(int value, String name) {
			this.value = value;
			this.name = name;
		}

		private int value;
		private String name = null;

		public int getValue() {
			return this.value;
		}

		public String getName() {
			return this.name;
		}

		public static FlowExecuteStatus valueOf(int status) {
			return getEnumByValue(FlowExecuteStatus.class, status);
		}
	}

	/**
	 * 执行容器的状态
	 * 
	 * @author Peter
	 *
	 */
	public static enum ExecuteContainerStatus {
		NORMAL(1, "正常"), // 正常的
		TIMEOUT(2, "心跳超时"), // 心跳超时的
		FORBBIDEN(3, "禁用"), // 被禁用的
		EXCEPTION(11, "异常"), // 发现有异常
		SHUTDOWN(110, "关闭"); // 关闭
		private ExecuteContainerStatus(int value, String name) {
			this.value = value;
			this.name = name;
		}

		private int value;
		private String name = null;

		public int getValue() {
			return this.value;
		}

		public String getName() {
			return this.name;
		}
	}

}
