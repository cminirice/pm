/**
 * 
 */
package com.guttv.pm.frame.jc;

import java.io.Closeable;
import java.io.IOException;

/**
 * @author Peter
 *
 */
public abstract class Parent implements Closeable {

	/* (non-Javadoc)
	 * @see java.io.Closeable#close()
	 */
	@Override
	public void close() throws IOException {
		System.out.println("Parent : " + this.getClass());
	}
	
	public void ff()  throws IOException{
		f2();
		this.close();
	}
	
	public abstract void f2() throws IOException;
	
	public static void main(String[]a) throws Exception{
		Parent p = new Son();
		p.ff();
		p.close();
	}

}
