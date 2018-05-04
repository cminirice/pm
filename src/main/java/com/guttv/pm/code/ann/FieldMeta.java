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
public @interface FieldMeta {
	
	//字段的中文名称
	public abstract String cn() default "";
	
	//字段的长度
	public abstract int length() default 128;
	
	//是不是必填字段
	public abstract boolean required() default false;
	
	//是否在列表页面展示
	public abstract boolean list() default false;
	
	//是否在编辑页面展示
	public abstract boolean edit() default true;
	
	//在列表页面的查询中出现的字段,一般情况 下，一个表需要至少一个字段可以查询
	public abstract boolean search () default false;
	
	//在列表页面是否可排序
	public abstract boolean sort() default false;
	
	
}
