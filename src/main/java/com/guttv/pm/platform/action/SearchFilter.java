/**
 * 
 */
package com.guttv.pm.platform.action;

import java.util.List;

/**
 * @author Peter
 *
 */
public interface SearchFilter<T> {

	/**
	 * 把某一个字段，查询对应的数据
	 * @param list  源集合
	 * @param filterField  需要过滤的字段
	 * @param value   按值过滤
	 * @return
	 */
	public List<T> filter(List<T> list,String filterField,String value);
}
