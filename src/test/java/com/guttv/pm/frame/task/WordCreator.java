/**
 * 
 */
package com.guttv.pm.frame.task;

import java.util.Random;

import com.guttv.pm.core.task.AbstractTask;

/**
 * @author Peter
 *
 */
public class WordCreator extends AbstractTask{
	public static void main(String[]a) {
		
		Random random = new Random(1);
		
		
		for(int i = 0; i < 10; i++) {
			System.out.println(random.nextInt(2));
		}
		
	}
	
	@Override
	public void dispose() throws Exception {
		String[] seed = {"com","guttv"};
		
		Random random = new Random(2);
		
		for(int i = 0; i < 10; i++) {
			this.writeData(seed[random.nextInt(2)]);
			Thread.sleep(1000);
		}
	}

}
