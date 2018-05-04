/**
 * 
 */
package com.guttv.pm.core.msg.rabbit;

import java.io.IOException;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.guttv.pm.core.cache.ConfigCache;
import com.guttv.pm.core.msg.queue.AbstractCheckRuleProducer;
import com.guttv.pm.utils.Constants;
import com.guttv.pm.utils.SerializeUtil;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.MessageProperties;

/**
 * @author Peter
 * 
 */
public class RabbitProducer extends AbstractCheckRuleProducer {
	protected Logger logger = LoggerFactory.getLogger("queue");
	private Channel channel = null;
	private Connection connection = null;

	public RabbitProducer(String queue) {

		int index = queue.indexOf("?");
		if (index >= 0) {
			if (index == 0) {
				this.setName("empty");
			} else {
				this.setName(StringUtils.trimToEmpty(queue.substring(0, index)));
			}

			String params = queue.substring(index + 1);
			String array[] = params.split("&");
			if (array != null && array.length > 0) {
				for (String param : array) {
					index = param.indexOf("=");
					if (index > 0) {
						String key = StringUtils.trimToEmpty(param.substring(0, index));
						String value = StringUtils.trimToEmpty(param.substring(index + 1));
						// 目前只支持下面几个参数
						if ("exchange".equalsIgnoreCase(key)) {
							exchange = value;
							continue;
						}
						if ("type".equalsIgnoreCase(key)) {
							type = value;
							continue;
						}
						if ("durable".equalsIgnoreCase(key)) {
							durable = !"false".equalsIgnoreCase(value); // 默认是true,只有为false时才为false
							continue;
						}
						if ("autoDelete".equalsIgnoreCase(key)) {
							autoDelete = "true".equalsIgnoreCase(value); // 默认是false,只有为true时才为true
							continue;
						}
						if ("exclusive".equalsIgnoreCase(key)) {
							exclusive = "true".equalsIgnoreCase(value); // 默认是false,只有为true时才为true
							continue;
						}
					}
				}
			}
		} else {
			this.setName(StringUtils.trimToEmpty(queue));
		}

		reinit();
	}

	private String name = null;

	@Override
	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public int size() {
		try {
			return (int) channel.messageCount(name);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			return 0;
		}
	}

	private short retryNum = 0;
	private short maxRetryNum = 3;

	/**
	 * exchange为空时，发送到指定的队列里 exchange不为空时，发送到该exchange，routingkey为空
	 */
	@Override
	public void write(Object data) throws Exception {
		if(data instanceof Map) {
			@SuppressWarnings("rawtypes")
			Map map = (Map)data;
			Object routingKey = map.get(RabbitConsumer.ROUTING_KEY);
			if(routingKey != null) {
				this.write(data, routingKey.toString());
				return;
			}
		}

		boolean flag = true; // 需要重试
		while (retryNum < maxRetryNum && flag) {
			try {

				if (StringUtils.isBlank(this.exchange)) {
					channel.basicPublish("", name, MessageProperties.PERSISTENT_TEXT_PLAIN, SerializeUtil.toByte(data));
				} else {
					channel.basicPublish(this.exchange, "", MessageProperties.PERSISTENT_TEXT_PLAIN,
							SerializeUtil.toByte(data));
				}

				flag = false;
				retryNum = 0; // 有成功的，就把重试恢复
			} catch (Exception e) {
				retryNum++;
				if (retryNum < maxRetryNum) {
					Thread.sleep(5000);
					logger.warn("出现异常，正在第[" + retryNum + "]次重新连接：" + e.getMessage());
					reinit();
				} else {
					throw e;
				}
			}
		}
	}

	/**
	 * 
	 * @param obj
	 * @param routingKey
	 *            exchange为空时，该参数无效
	 */
	public void write(Object data, String routingKey) throws Exception {

		boolean flag = true; // 需要重试
		while (retryNum < maxRetryNum && flag) {
			try {

				// 如果没有交换机信息，不发送routingKey
				if (StringUtils.isBlank(this.exchange)) {
					channel.basicPublish("", name, MessageProperties.PERSISTENT_TEXT_PLAIN, SerializeUtil.toByte(data));
				} else {
					channel.basicPublish(this.exchange, routingKey, MessageProperties.PERSISTENT_TEXT_PLAIN,
							SerializeUtil.toByte(data));
				}

				flag = false;
				retryNum = 0; // 有成功的，就把重试恢复
			} catch (Exception e) {
				retryNum++;
				if (retryNum < maxRetryNum) {
					Thread.sleep(5000);
					logger.warn("出现异常，正在第[" + retryNum + "]次重新连接：" + e.getMessage());
					reinit();
				} else {
					throw e;
				}
			}
		}

	}

	@Override
	public void close() throws IOException {
		try {
			if (channel != null) {
				channel.close();
			}
		} catch (Exception e) {
		}
		try {
			if (connection != null) {
				connection.close();
			}
		} catch (Exception e) {
		}
	}

	public void reinit() {
		String addr = ConfigCache.getInstance().getProperty(Constants.RABBIT_ADDR, null);
		try {
			this.close();

			// 设置MabbitMQ所在主机ip或者主机名

			ConnectionFactory factory = new ConnectionFactory();

			factory.setUri(addr);

			connection = factory.newConnection();

			channel = connection.createChannel();

			if (StringUtils.isNotBlank(exchange)) {
				ExchangeType t = null;
				if (StringUtils.isBlank(type)) {
					t = ExchangeType.FANOUT;
				} else {
					t = Enum.valueOf(ExchangeType.class, type.toUpperCase());
				}
				channel.exchangeDeclare(exchange.trim(), t.getValue(), this.durable);
			}

			if (!"null".equalsIgnoreCase(name) && !"empty".equalsIgnoreCase(name)) {
				channel.queueDeclare(name, this.durable, exclusive, autoDelete, null);
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
	}

	/*
	 * public void reinit() { String addr =
	 * ConfigCache.getInstance().getProperty(Constants.RABBIT_ADDR, null); try {
	 * this.close();
	 * 
	 * ConnectionFactory factory = new ConnectionFactory();
	 * factory.setUri(addr); connection = factory.newConnection(); channel =
	 * connection.createChannel(); channel.queueDeclare(name, true, false,
	 * false, null); channel.basicQos(1); } catch (Exception e) {
	 * logger.error(e.getMessage(), e); } }
	 */

	private String exchange = "";
	private String type = ""; // exchenge的类型
	private boolean durable = true;
	private boolean autoDelete = false;
	private boolean exclusive = false;
}
