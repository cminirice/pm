/**
 * 
 */
package com.guttv.pm.support.ann;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 为某些类型的任务强制添加可读操作，需要组件和任务支持
 * 
 * @author Peter
 *
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Readable {
}
