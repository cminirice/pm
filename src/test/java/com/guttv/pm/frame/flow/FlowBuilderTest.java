/**
 * 
 */
package com.guttv.pm.frame.flow;

import org.apache.commons.lang3.RandomStringUtils;

import com.google.gson.Gson;
import com.guttv.pm.core.bean.ComponentPackageBean;
import com.guttv.pm.core.bean.FlowBean;
import com.guttv.pm.core.cache.FlowCache;
import com.guttv.pm.core.flow.ComPackRegisteManager;
import com.guttv.pm.core.flow.FlowExecuteConfig;
import com.guttv.pm.core.flow.FlowExecuteConfigBuilder;

import junit.framework.TestCase;

/**
 * @author Peter
 *
 */
public class FlowBuilderTest extends TestCase {

	
	public void testBuild() throws Exception{
		cacheComponent();
		
		String gooFlowContent = "{\"title\":\"流程名称12\",\"nodes\":{\"demo_node_26\":{\"name\":\"创建字符\",\"left\":336,\"top\":158,\"type\":\"node\",\"width\":102,\"height\":24,\"alt\":true},\"demo_node_27\":{\"name\":\"统计GUTTV\",\"left\":122,\"top\":286,\"type\":\"node\",\"width\":101,\"height\":24,\"alt\":true},\"demo_node_54\":{\"name\":\"统计com\",\"left\":580,\"top\":313,\"type\":\"node\",\"width\":102,\"height\":24,\"alt\":true},\"1508911962877\":{\"name\":\"虚任务\",\"left\":331,\"top\":37,\"type\":\"node\",\"width\":102,\"height\":24,\"alt\":true}},\"lines\":{\"demo_line_46\":{\"type\":\"sl\",\"from\":\"demo_node_26\",\"to\":\"demo_node_27\",\"name\":\"DATA.equals(\\\"guttv\\\")\"},\"demo_line_55\":{\"type\":\"sl\",\"from\":\"demo_node_26\",\"to\":\"demo_node_54\",\"name\":\"DATA.equals(\\\"com\\\")\"},\"1508912318636\":{\"type\":\"sl\",\"from\":\"demo_node_26\",\"to\":\"1508911962877\",\"name\":\"\",\"alt\":true}},\"areas\":{},\"initNum\":58}";
		String flowComponentPros = "{\"demo_node_26\":{\"age\":\"4\",\"name\":\"peter\"},\"demo_node_54\":{\"period\":\"2\",\"maxCount\":\"100\"},\"demo_node_27\":{\"pause\":\"false\",\"filePath\":\"/data/pm\",\"filterWord\":\"shit\",\"period\":\"5\"},\"1508911962877\":{}}";
		String nodeVSComponent = "{\"demo_node_26\":\"com.guttv.pm.frame.task.WordCreator\",\"demo_node_54\":\"com.guttv.pm.frame.task.ComWordCount\",\"demo_node_27\":\"com.guttv.pm.frame.task.GuttvWordCount\",\"1508911962877\":\"\"}";
		
		FlowBean flow = new FlowBean();
		flow.setCode(RandomStringUtils.randomNumeric(16));
		flow.setFlowContent(gooFlowContent);
		flow.setFlowComPros(flowComponentPros);
		flow.setNodeVSCom(nodeVSComponent);
		
		FlowCache.getInstance().cacheFlow(flow);
		
		FlowExecuteConfig flowConfig = FlowExecuteConfigBuilder.build(flow.getCode(),flow.getFlowContent(),flow.getFlowComPros(),flow.getNodeVSCom());
		
		System.out.println(flowConfig.getFlowCode());
		
		Gson gson = new Gson();
		System.out.println(gson.toJson(flowConfig));
	}
	
	private void cacheComponent() throws Exception{
		
		ComponentPackageBean comPack = new ComponentPackageBean();
		comPack.setComPackageFilePath("D:\\data\\task\\task-word.zip");
		
		ComPackRegisteManager.getInstance().registComPack(comPack);
		
		/*ComponentBean task = new ComponentBean();
		task.setName("WordCreator");
		task.setCn("创建字符");
		task.setClz("com.guttv.pm.frame.task.WordCreator");
		task.setNeedRead(true);
		task.setQueueType("native://");
		List<ComponentProBean> taskPros = new ArrayList<ComponentProBean>();
		task.setComponentPros(taskPros);
		ComponentProBean taskPro = new ComponentProBean();
		taskPros.add(taskPro);
		taskPro.setCn("");
		taskPro.setType(1);
		taskPro.setName("age");
		taskPro.setValue("4");
		taskPro = new ComponentProBean();
		taskPros.add(taskPro);
		taskPro.setCn("");
		taskPro.setType(1);
		taskPro.setName("name");
		taskPro.setValue("peter");
		taskPro.setCreateTime(new Date());
		taskPro.setUpdateTime(new Date());
		ComponentCache.getInstance().cacheComponent(task);
		
		
		task = new ComponentBean();
		task.setName("ComWordCount");
		task.setCn("COM统计");
		task.setClz("com.guttv.pm.frame.task.ComWordCount");
		task.setNeedWrite(false);
		task.setQueueType("native://");
		taskPros = new ArrayList<ComponentProBean>();
		task.setComponentPros(taskPros);
		taskPro = new ComponentProBean();
		taskPros.add(taskPro);
		taskPro.setCn("");
		taskPro.setType(1);
		taskPro.setName("period");
		taskPro.setValue("4");
		taskPro = new ComponentProBean();
		taskPros.add(taskPro);
		taskPro.setCn("");
		taskPro.setType(1);
		taskPro.setName("maxCount");
		taskPro.setValue("100");
		taskPro = new ComponentProBean();
		taskPros.add(taskPro);
		taskPro.setCn("");
		taskPro.setType(2);
		taskPro.setName("filePath");
		taskPro.setValue("/");
		ComponentCache.getInstance().cacheComponent(task);
		
		
		task = new ComponentBean();
		task.setName("GuttvWordCount");
		task.setCn("Guttv统计");
		task.setClz("com.guttv.pm.frame.task.GuttvWordCount");
		task.setNeedWrite(false);
		task.setQueueType("native://");
		taskPros = new ArrayList<ComponentProBean>();
		task.setComponentPros(taskPros);
		taskPro = new ComponentProBean();
		taskPros.add(taskPro);
		taskPro.setCn("");
		taskPro.setType(1);
		taskPro.setName("pause");
		taskPro.setValue("false");
		taskPro = new ComponentProBean();
		taskPros.add(taskPro);
		taskPro.setCn("");
		taskPro.setType(1);
		taskPro.setName("filePath");
		taskPro.setValue("/data/pm");
		taskPro = new ComponentProBean();
		taskPros.add(taskPro);
		taskPro.setCn("");
		taskPro.setType(1);
		taskPro.setName("period");
		taskPro.setValue("5");
		ComponentCache.getInstance().cacheComponent(task);*/
	}
}
