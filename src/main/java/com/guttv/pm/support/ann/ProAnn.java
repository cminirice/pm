/**
 * 
 */
package com.guttv.pm.support.ann;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author Peter
 *
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ProAnn {

	ProType type() default ProType.NOR;

	/**
	 * 中文名称
	 * 
	 * @return
	 */
	String cn() default "";

	/**
	 * 属性的类型，NOR：基本类配置；DEV：流程控制配置
	 * 
	 * @author Peter
	 *
	 */
	public enum ProType {
		NOR, DEV;
	}
}
