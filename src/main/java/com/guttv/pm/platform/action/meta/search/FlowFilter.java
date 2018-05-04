/**
 * 
 */
package com.guttv.pm.platform.action.meta.search;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.tomcat.util.file.Matcher;

import com.guttv.pm.core.bean.FlowBean;
import com.guttv.pm.platform.action.SearchFilter;

/**
 * @author Peter
 *
 */
public class FlowFilter implements SearchFilter<FlowBean> {

	/*
	 * (non-Javadoc)
	 * @see com.guttv.pm.view.action.meta.search.SearchFilter#filter(java.util.List, java.lang.String, java.lang.String)
	 */
	@Override
	public List<FlowBean> filter(List<FlowBean> list, String filterField, String value) {

		if(StringUtils.isBlank(filterField) || StringUtils.isBlank(value) || list == null || list.size() == 0) {
			return list;
		}
		
		List<FlowBean> flows = new ArrayList<FlowBean>();
		
		String pattern = "*"+value+"*";
		//循环查找吧
		for(FlowBean flow : list) {
			
			//分为同的字段，不同的查询方法
			if(filterField.equals("code")) {
				if(StringUtils.isNotBlank(flow.getCode()) && Matcher.match(pattern, flow.getCode(), false)) {
					flows.add(flow);
				}
			}else if(filterField.equals("name")) {
				if(StringUtils.isNotBlank(flow.getName()) && Matcher.match(pattern, flow.getName(), false)) {
					flows.add(flow);
				}
			} else {
				//在没有实现的情况下，把所有的返回
				flows.add(flow);
			}
		}
		
		return flows;
	}

}
