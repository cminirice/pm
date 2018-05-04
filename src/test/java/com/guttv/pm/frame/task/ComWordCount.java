/**
 * 
 */
package com.guttv.pm.frame.task;

import com.guttv.pm.core.task.AbstractRecycleTask;

/**
 * @author Peter
 *
 */
public class ComWordCount extends AbstractRecycleTask {

	private int ccount = 0;
	@Override
	public Object dispose(Object data) throws Exception {
		if(data == null) {
			return null;
		}else if(data.toString().equalsIgnoreCase("com")) {
			ccount++;
			logger.debug("comcomcomcomcomcomcomcomcomcomcomcomcomcomcomcom收到com个数：" + ccount);
		}else {
			logger.debug("收到错误数据：" + data);
		}
		return null;
	}

}
