/**
 * 
 */
package com.guttv.rpc.common;

import com.guttv.pm.utils.SerializeUtil;

/**
 * @author Peter
 *
 */
public class TestSerializationUtil {

	public static void main(String[] a) throws Exception {

		/*
		 * [10000]次SerializationUtil.serialize用时：168
		 * [10000]次SerializationUtil.deserialize用时：12
		 * [10000]次SerializeUtil.toByte用时：60 [10000]次SerializeUtil.toObj用时：147
		 */

		/*
		 * [100000]次SerializationUtil.serialize用时：159
		 * [100000]次SerializationUtil.deserialize用时：17
		 * [100000]次SerializeUtil.toByte用时：269
		 * [100000]次SerializeUtil.toObj用时：746
		 */

		/*
		 * 测试时发现第一次调用SerializationUtil的方法慢，后面的很快 测试前先调用一次
		 * SerializationUtil.serialize(request); 速度快10倍以上
		 * [10000]次SerializationUtil.serialize用时：7
		 * [10000]次SerializationUtil.deserialize用时：12
		 * [10000]次SerializeUtil.toByte用时：75 [10000]次SerializeUtil.toObj用时：177
		 */

		int times = 10000;
		byte[] bytes = null;
		RpcRequest request = new RpcRequest();
		SerializationUtil.serialize(request);

		long start = System.currentTimeMillis();
		for (int i = 0; i < times; i++) {
			bytes = SerializationUtil.serialize(request);
		}
		System.out.println("[" + times + "]次SerializationUtil.serialize用时：" + (System.currentTimeMillis() - start));

		start = System.currentTimeMillis();
		for (int i = 0; i < times; i++) {
			SerializationUtil.deserialize(bytes, RpcRequest.class);
		}
		System.out.println("[" + times + "]次SerializationUtil.deserialize用时：" + (System.currentTimeMillis() - start));

		start = System.currentTimeMillis();
		for (int i = 0; i < times; i++) {
			bytes = SerializeUtil.toByte(request);
		}
		System.out.println("[" + times + "]次SerializeUtil.toByte用时：" + (System.currentTimeMillis() - start));

		start = System.currentTimeMillis();
		for (int i = 0; i < times; i++) {
			SerializeUtil.toObj(bytes);
		}
		System.out.println("[" + times + "]次SerializeUtil.toObj用时：" + (System.currentTimeMillis() - start));

		// 下面是证明两种序列化的数据是可以互用的
		System.out.println(SerializationUtil.deserialize(SerializeUtil.toByte(SerializationUtil.serialize(SerializeUtil.toObj(bytes))), RpcRequest.class));

	}
}
