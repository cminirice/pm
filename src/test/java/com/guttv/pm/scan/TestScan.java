/**
 * 
 */
package com.guttv.pm.scan;

import java.io.File;
import java.net.URL;

import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;

import com.guttv.pm.core.flow.ComponentClassLoader;
import com.guttv.pm.utils.ReflectUtil;

/**
 * @author Peter
 *
 */
public class TestScan {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		final ComponentClassLoader loader = new ComponentClassLoader(
				new URL[] { new File("D:\\data\\task\\scan-v1.0.jar").toURI().toURL() });
		loader.tryInitApplicationContext("domain");
		loader.scan("com.guttv.component.scan","com.guttv.component.scan","com.guttv.component.test");
		Object obj = loader.getBean("scanService");
		System.out.println(obj);
		obj = loader.newInstance("com.guttv.component.scan.ScanService");
		System.out.println(obj);
		
		System.out.println(obj == loader.newInstance("com.guttv.component.scan.ScanService"));
		
		obj = loader.getBean("person");
		System.out.println(obj);
		if(true) {
			return;
		}
		
		ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext();
		context.setClassLoader(loader);
		context.setConfigLocation("classpath:*domain-spring.xml");
		context.refresh();
		
		//Object obj = context.getBean("scanService");

		System.out.println(obj);
		System.out.println(ReflectUtil.getFieldValue(obj, "dao"));

	}

}

class MyClassPathXmlApplicationContext extends ClassPathXmlApplicationContext{
	public MyClassPathXmlApplicationContext(ClassLoader loader) {
		this.setClassLoader(loader);
	}
	protected ResourcePatternResolver getResourcePatternResolver() {
		return new PathMatchingResourcePatternResolver(this.getClassLoader());
	}
}
