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
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface ComponentAnn {

	// comID
	String comID() default "";

	//所属组
	String group() default "default";
	
	//组件的中文名称
	String cn() default "";
	
	//组件的名称
	String name() default "";
	
	//接收消息的通道
	String receive() default "";
	
	//描述信息
	String description() default "";
}
