/**
 * 
 */
package com.guttv.pm.core.msg.curator;

import java.io.IOException;

import org.apache.curator.framework.recipes.queue.QueueSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.guttv.pm.utils.SerializeUtil;

/**
 * @author Peter
 *
 */
public class DataPackageSerializer implements QueueSerializer<Object>{
	protected Logger logger = LoggerFactory.getLogger("task");
	//private Gson gson = new Gson();
	
	//private String encoding = "UTF-8";
	/**
	 * 反序列化
	 */
	@Override
	public Object deserialize(byte[] bytes) {
		/*try {
			return gson.fromJson(new String(bytes,encoding), HashMap.class);
		} catch (JsonSyntaxException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}*/
		try {
			return SerializeUtil.toObj(bytes);
		}catch(IOException e) {
			logger.error("反序列化对象失败：" + e.getMessage(), e);
		} catch (ClassNotFoundException e) {
			logger.error("反序列号对应失败，找不到目标类：" + e.getMessage(), e);
		}
		return null;
	}

	/**
	 * 序列化
	 */
	@Override
	public byte[] serialize(Object data) {
		if(data == null) return null;
		//return gson.toJson(data).getBytes();
		try {
			return SerializeUtil.toByte(data);
		} catch (IOException e) {
			logger.error("序列化对象失败：" + e.getMessage(), e);
		}
		return null;
	}

}
