/**
 * 
 */
package com.guttv.pm.frame.jc;

import java.io.IOException;

/**
 * @author Peter
 *
 */
public class Son extends Parent {
	public void close() throws IOException {
		System.out.println("Son : " + this.getClass());
		super.close();
	}
	
	public void f2() throws IOException{
		this.close();
	}
}
