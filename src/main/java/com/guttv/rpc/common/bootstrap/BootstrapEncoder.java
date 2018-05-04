/**
 * 
 */
package com.guttv.rpc.common.bootstrap;

import com.guttv.rpc.common.SerializationUtil;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

/**
 * @author Peter
 *
 */
public class BootstrapEncoder extends MessageToByteEncoder<Object> {

	private Class<?> genericClass;

	public BootstrapEncoder(Class<?> genericClass) {
		this.genericClass = genericClass;
	}

	@Override
	public void encode(ChannelHandlerContext ctx, Object in, ByteBuf out) throws Exception {
		if (genericClass.isInstance(in)) {
			byte[] data = SerializationUtil.serialize(in);
			out.writeInt(data.length);
			out.writeBytes(data);
		}
	}
}
