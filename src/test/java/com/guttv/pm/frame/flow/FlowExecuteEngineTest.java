/**
 * 
 */
package com.guttv.pm.frame.flow;

import java.io.File;

import org.apache.commons.lang3.RandomStringUtils;

import com.guttv.pm.core.bean.ComponentPackageBean;
import com.guttv.pm.core.bean.FlowBean;
import com.guttv.pm.core.cache.FlowCache;
import com.guttv.pm.core.cache.FlowExecuteConfigCache;
import com.guttv.pm.core.flow.ComPackRegisteManager;
import com.guttv.pm.core.flow.FlowExecuteConfig;
import com.guttv.pm.core.flow.FlowExecuteConfigBuilder;
import com.guttv.pm.core.flow.FlowExecuteEngine;

import junit.framework.TestCase;

/**
 * @author Peter
 *
 */
public class FlowExecuteEngineTest extends TestCase {

	public void testExecute() {
		try {
			
			ComponentPackageBean comPack = ComPackRegisteManager.getInstance().uploadAndCreate(null,new File("D:\\data\\task\\word_count-v1.0.jar"));
			ComPackRegisteManager.getInstance().registComPack(comPack);
			
			comPack = ComPackRegisteManager.getInstance().uploadAndCreate(null,new File("D:\\data\\task\\word_create-v1.0.jar"));
			ComPackRegisteManager.getInstance().registComPack(comPack);
			
			
			String gooFlowContent = "{\"title\":\"字符串统计测试\",\"nodes\":{\"1509614450185\":{\"name\":\"字符工厂\",\"left\":349,\"top\":77,\"type\":\"node\",\"width\":101,\"height\":24,\"alt\":true},\"1509614470258\":{\"name\":\"字符统计\",\"left\":231,\"top\":203,\"type\":\"node\",\"width\":102,\"height\":24,\"alt\":true},\"1509614471785\":{\"name\":\"字符统计\",\"left\":508,\"top\":216,\"type\":\"node\",\"width\":102,\"height\":24,\"alt\":true}},\"lines\":{\"1509614508403\":{\"type\":\"sl\",\"from\":\"1509614450185\",\"to\":\"1509614470258\",\"name\":\"DATA.equals(\\\"com\\\")\",\"alt\":true},\"1509614512194\":{\"type\":\"sl\",\"from\":\"1509614450185\",\"to\":\"1509614471785\",\"name\":\"DATA.equals(\\\"guttv\\\")\",\"alt\":true}},\"areas\":{},\"initNum\":6}";
			String flowComponentPros = "{\"1509614450185\":{\"maxTimes\":\"100\"},\"1509614470258\":{\"logPeriod\":\"3\"},\"1509614471785\":{\"logPeriod\":\"4\"}}";
			String nodeVSComponent = "{\"1509614450185\":\"com.guttv.component.word.WordFactory\",\"1509614470258\":\"com.guttv.component.word.WordCount\",\"1509614471785\":\"com.guttv.component.word.WordCount\"}";
			
			FlowBean flow = new FlowBean();
			flow.setCode(RandomStringUtils.randomNumeric(16));
			flow.setFlowContent(gooFlowContent);
			flow.setFlowComPros(flowComponentPros);
			flow.setNodeVSCom(nodeVSComponent);
			
			FlowCache.getInstance().cacheFlow(flow);
			
			/*FlowExecuteConfig flowConfig = FlowExecuteConfigBuilder.build(flow.getCode(),flow.getFlowContent(),flow.getFlowComPros(),flow.getNodeVSCom());
			flowConfig.setFlowExeCode(RandomStringUtils.randomNumeric(16));
			*/
			FlowExecuteConfig flowConfig = FlowExecuteConfigBuilder.build(flow.getCode());
			
			FlowExecuteConfigCache.getInstance().cacheFlowExecuteConfig(flowConfig);
			
			FlowExecuteEngine.excuteFlowExecuteConfig(flowConfig);
			
			Thread.sleep(3000);
			
			FlowExecuteEngine.stopFlowExecuteConfig(flowConfig);
			
			while(true) {
				Thread.sleep(1000);
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
}
