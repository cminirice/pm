/**
 * 
 */
package com.guttv.pm.frame.flow;

import java.io.File;
import java.util.List;

import com.google.gson.Gson;
import com.guttv.pm.core.bean.ComponentBean;
import com.guttv.pm.core.bean.ComponentPackageBean;
import com.guttv.pm.core.flow.cb.LoadComponentFromConfigFile;

import junit.framework.TestCase;

/**
 * @author Peter
 *
 */
public class TaskBuilderTest extends TestCase {

	public void test() throws Exception{
		ComponentPackageBean taskRegist = new ComponentPackageBean();
		LoadComponentFromConfigFile.loadFrom(new File("D:\\workspace2016\\pm\\src\\test\\java\\com\\guttv\\pm\\frame\\task\\test-task.xml"),taskRegist);
		List<ComponentBean> tasks = taskRegist.getComponents();
		Gson gson = new Gson();
		System.out.println(gson.toJson(tasks));
	}
}
