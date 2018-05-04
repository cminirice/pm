/**
 * 
 */
package com.guttv.pm.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * @author Peter
 *
 */
public class SerializeUtil {
	
	public static Object toObj(byte[] bytes) throws IOException,ClassNotFoundException{
        // 反序列化
		ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
        ObjectInputStream ois = new ObjectInputStream(bais);
        return ois.readObject();
	}

	/**
	 * 序列化对象
	 * @param obj
	 * @return
	 * @throws IOException
	 */
	public static byte[] toByte(Object obj) throws IOException {
		ObjectOutputStream oos = null;
        ByteArrayOutputStream baos = null;
        // 序列化
        byte[] bytes = null;

		baos = new ByteArrayOutputStream();
		oos = new ObjectOutputStream(baos);
		oos.writeObject(obj);
		bytes = baos.toByteArray();
		
        return bytes;
	}
}
