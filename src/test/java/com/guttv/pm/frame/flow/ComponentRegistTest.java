/**
 * 
 */
package com.guttv.pm.frame.flow;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.List;

import com.guttv.pm.utils.Decompression;

import junit.framework.TestCase;

/**
 * @author Peter
 *
 */
public class ComponentRegistTest extends TestCase{

	public void testRegistFromJar() throws Exception{
		URLClassLoader urlClassLoader = null;
		try {
			File jarFile = new File("D:\\data\\task\\task-word\\task-word.jar");
			
			List<String> paths = Decompression.getResource(new File("D:\\data\\task\\task-word\\task-word.jar"),".*com.xml");
			System.out.println(paths);
			
			urlClassLoader = new URLClassLoader(new URL[] {jarFile.toURI().toURL()});
			
			System.out.println(urlClassLoader.loadClass("com.guttv.pm.frame.task.GuttvWordCount"));
			URL source = urlClassLoader.getResource("com/guttv/pm/frame/task/test-com.xml");
			System.out.println("*************" + source);
			
			source = urlClassLoader.findResource(paths.get(0));
			System.out.println("############" + source);
			
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			urlClassLoader.close();
		}
	}
}
