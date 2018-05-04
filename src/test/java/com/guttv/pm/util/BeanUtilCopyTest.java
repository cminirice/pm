package com.guttv.pm.util;

import java.util.Date;

import org.springframework.beans.BeanUtils;

import com.google.gson.Gson;
import com.guttv.pm.core.bean.ComponentFlowProBean;
import com.guttv.pm.core.bean.ComponentProBean;
import com.guttv.pm.utils.Enums.ComponentProType;

import junit.framework.TestCase;

public class BeanUtilCopyTest extends TestCase{

	public void testCopy() throws Exception{
		ComponentProBean tp = new ComponentProBean();
		
		tp.setComponentClz("com.guttv.bean.GuttvServer");
		tp.setCn("下载");
		tp.setCreateTime(new Date());
		tp.setName("time_out");
		tp.setType(ComponentProType.DEV);
		tp.setUpdateTime(new Date());
		tp.setValue("50");
		
		ComponentFlowProBean tfp = new ComponentFlowProBean();
		System.out.println("1:" + System.currentTimeMillis());
		BeanUtils.copyProperties(tp, tfp);
		System.out.println("2:" + System.currentTimeMillis());
		Gson gson = new Gson();
		System.out.println(gson.toJson(tfp));
	}
	
	public void testCopy1() {
		ComponentProBean tp = new ComponentProBean();
		
		tp.setComponentClz("com.guttv.bean.GuttvServer");
		tp.setCn("下载");
		tp.setCreateTime(new Date());
		tp.setName("time_out");
		tp.setType(ComponentProType.NOR);
		tp.setUpdateTime(new Date());
		tp.setValue("50");
		
		ComponentFlowProBean tfp = new ComponentFlowProBean();
		System.out.println("3:" + System.currentTimeMillis());
		tfp.setComponentClz(tp.getComponentClz());
		tp.setType(ComponentProType.NOR);
		tfp.setCn(tp.getCn());
		tfp.setName(tp.getName());
		tfp.setValue(tp.getValue());
		tfp.setCreateTime(tp.getCreateTime());
		tfp.setUpdateTime(tp.getUpdateTime());
		System.out.println("4:" + System.currentTimeMillis());
		Gson gson = new Gson();
		System.out.println(gson.toJson(tfp));
	}
}
