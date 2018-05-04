/**
 * 
 */
package com.guttv.pm.support.ann;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 组件要执行的方法
 * 
 * @author Peter
 *
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ExecuteMethod {

	/**
	 * 方法执行的类型，有周期和单次，默认是单次
	 * 
	 * @return
	 */
	public abstract ExecuteType type() default ExecuteType.Once;

	public enum ExecuteType {
		Cycle("cycle"), Once("once"), Scheduler("scheduler"), Rest("rest");
		private ExecuteType(String value) {
			this.value = value;
		}

		private String value;

		public String getValue() {
			return this.value;
		}
	}
}
