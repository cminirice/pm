/**
 * 
 */
package com.guttv.pm.core.task;

import com.guttv.pm.core.msg.queue.AbstractDispatchRule;

import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import groovy.lang.Script;

/**
 * @author Peter
 *
 */
public class SimpleDispathRule extends AbstractDispatchRule{
	private Script script = null;
	
	/**
	    String rule = "DATA.get('type')==2";
		Map<String,Integer> map = new HashMap<String,Integer>();
		map.put("type", 2);
		SimpleDispathRule simpleRule = new SimpleDispathRule(rule);
		System.out.println(simpleRule.check(map));
		
		注意rule中的类型要一致，如果 map.put("type","2") rule="DATA.get('type')=='2'"  才能计算出来true
		
	 * @param rule
	 */
	public SimpleDispathRule(String rule) {
		GroovyShell shell = new GroovyShell();
		script = shell.parse(rule);
	}
	
	public boolean check(Object data) throws Exception{
		if(data == null) return false;
		Binding binding = new Binding();
		binding.setVariable("DATA",data);
		script.setBinding(binding);
		Object obj = script.run();
		
		if(obj == null) {
			return false;
		}else {
			return Boolean.parseBoolean(obj.toString());
		}
		
	}
	
}
