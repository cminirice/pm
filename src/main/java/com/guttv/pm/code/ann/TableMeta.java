/**
 * 
 */
package com.guttv.pm.code.ann;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * @author Peter
 *
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface TableMeta {

	//表的中文名称
	public abstract String cn() default "";
	
	//表的包名，对应struts的包名
	public abstract String pkg() default "";
}
