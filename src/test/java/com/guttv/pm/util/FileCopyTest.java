/**
 * 
 */
package com.guttv.pm.util;

import java.io.File;

import org.apache.commons.io.FileUtils;

/**
 * @author Peter
 *
 */
public class FileCopyTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		FileUtils.copyDirectory(new File("D:\\data\\logs\\pm"), new File("D:\\data\\logs\\bt"));

	}

}
