package com.guttv.rpc.common;

import java.util.concurrent.ConcurrentHashMap;

import org.objenesis.Objenesis;
import org.objenesis.ObjenesisStd;

import com.dyuproject.protostuff.LinkedBuffer;
import com.dyuproject.protostuff.ProtostuffIOUtil;
import com.dyuproject.protostuff.Schema;
import com.dyuproject.protostuff.runtime.RuntimeSchema;

/**
 * 
 * 第一次序列化时用 100 ms ，后面序列化会快，比用java.io序列化（）快10倍以上。
 * 
 * 
 * @author Peter
 *
 */
public class SerializationUtil {

	private static ConcurrentHashMap<Class<?>, Schema<?>> cachedSchema = new ConcurrentHashMap<>();

	private static Objenesis objenesis = new ObjenesisStd(true);

	private SerializationUtil() {
	}

	@SuppressWarnings("unchecked")
	private static <T> Schema<T> getSchema(Class<T> cls) {
		Schema<T> schema = (Schema<T>) cachedSchema.get(cls);
		if (schema == null) {
			schema = RuntimeSchema.createFrom(cls);
			if (schema != null) {
				Schema<T> synSchema = (Schema<T>) cachedSchema.putIfAbsent(cls, schema);
				schema = synSchema == null ? schema : synSchema;
			}
		}
		return schema;
	}

	@SuppressWarnings("unchecked")
	public static <T> byte[] serialize(T obj) {
		Class<T> cls = (Class<T>) obj.getClass();
		LinkedBuffer buffer = LinkedBuffer.allocate(LinkedBuffer.DEFAULT_BUFFER_SIZE);
		try {
			Schema<T> schema = getSchema(cls);
			return ProtostuffIOUtil.toByteArray(obj, schema, buffer);
		} finally {
			if (buffer != null) {
				buffer.clear();
			}
		}
	}

	public static <T> T deserialize(byte[] data, Class<T> cls) {
		T message = objenesis.newInstance(cls);
		Schema<T> schema = getSchema(cls);
		ProtostuffIOUtil.mergeFrom(data, message, schema);
		return message;
	}
}
