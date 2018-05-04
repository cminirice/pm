/**
 * 
 */
package com.guttv.pm.platform.action.meta.search;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.tomcat.util.file.Matcher;

import com.guttv.pm.core.flow.FlowExecuteConfig;
import com.guttv.pm.platform.action.SearchFilter;

/**
 * @author Peter
 *
 */
public class FlowExecuteConfigFilter implements SearchFilter<FlowExecuteConfig> {

	/*
	 * (non-Javadoc)
	 * @see com.guttv.pm.view.action.meta.search.SearchFilter#filter(java.util.List, java.lang.String, java.lang.String)
	 */
	@Override
	public List<FlowExecuteConfig> filter(List<FlowExecuteConfig> list, String filterField, String value) {

		if(StringUtils.isBlank(filterField) || StringUtils.isBlank(value) || list == null || list.size() == 0) {
			return list;
		}
		
		List<FlowExecuteConfig> flowExecuteConfigs = new ArrayList<FlowExecuteConfig>();
		
		String pattern = "*"+value+"*";
		//循环查找吧
		for(FlowExecuteConfig flowExecuteConfig : list) {
			
			//分为同的字段，不同的查询方法
			if(filterField.equals("flowExeCode")) {
				if(StringUtils.isNotBlank(flowExecuteConfig.getFlowExeCode()) && Matcher.match(pattern, flowExecuteConfig.getFlowExeCode(), false)) {
					flowExecuteConfigs.add(flowExecuteConfig);
				}
			}else if(filterField.equals("flowCode")) {
				if(StringUtils.isNotBlank(flowExecuteConfig.getFlowCode()) && Matcher.match(pattern, flowExecuteConfig.getFlowCode(), false)) {
					flowExecuteConfigs.add(flowExecuteConfig);
				}
			}else if(filterField.equals("flowName")) {
				if(StringUtils.isNotBlank(flowExecuteConfig.getFlowName()) && Matcher.match(pattern, flowExecuteConfig.getFlowName(), false)) {
					flowExecuteConfigs.add(flowExecuteConfig);
				}
			}  else {
				//在没有实现的情况下，把所有的返回
				flowExecuteConfigs.add(flowExecuteConfig);
			}
		}
		
		return flowExecuteConfigs;
	}

}
