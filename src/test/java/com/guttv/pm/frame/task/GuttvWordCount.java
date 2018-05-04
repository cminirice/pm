/**
 * 
 */
package com.guttv.pm.frame.task;

import com.guttv.pm.core.task.AbstractRecycleTask;

/**
 * @author Peter
 *
 */
public class GuttvWordCount  extends AbstractRecycleTask {

	private int gcount = 0;
	@Override
	public Object dispose(Object data) throws Exception {
		if(data == null) {
			return null;
		}else if(data.toString().equalsIgnoreCase("guttv")) {
			gcount++;
			logger.debug("guttvguttvguttvguttvguttvguttvguttvguttvguttvguttv收到guttv个数：" + gcount);
		}else {
			logger.debug("收到错误数据：" + data);
		}
		return null;
	}
}
