/**
 * 
 */
package com.guttv.pm.core.msg.rabbit;

import java.io.IOException;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.guttv.pm.core.cache.ConfigCache;
import com.guttv.pm.core.msg.queue.Consumer;
import com.guttv.pm.utils.Constants;
import com.guttv.pm.utils.SerializeUtil;
import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;

/**
 * 该类的read 方法必须配合fallback或者commit方法使用，否则，会一直处理一条数据，或者读取数据为空
 * 
 * 该类不是线程安全的，多线程使用同一个对象是非常危险的
 * 
 * @author Peter
 * 
 */
public class RabbitConsumer implements Consumer {
	protected Logger logger = LoggerFactory.getLogger("queue");
	private Channel channel = null;
	private Connection connection = null;

	private com.rabbitmq.client.Consumer consumer = null;
	
	public static final String ROUTING_KEY = "routingKey";

	public RabbitConsumer(String queue) {

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
						if (ROUTING_KEY.equalsIgnoreCase(key)) {
							routingKey = value;
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.io.Closeable#close()
	 */
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.guttv.common.msg.queue.Consumer#setName(java.lang.String)
	 */
	@Override
	public void setName(String name) {
		this.name = name;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.guttv.common.msg.queue.Consumer#getName()
	 */
	@Override
	public String getName() {
		return name;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.guttv.common.msg.queue.Consumer#read()
	 */
	@Override
	public Object read() throws Exception {
		return read(0);
	}

	private long currentDeliveryTag = -1;

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.guttv.common.msg.queue.Consumer#read(int)
	 */
	@Override
	public Object read(int wait) throws Exception {

		long start = System.currentTimeMillis();

		while (data == null && (System.currentTimeMillis() - start) < wait) {
			Thread.sleep(10);
		}

		Object obj = null;
		synchronized (lock) {
			obj = data;
			data = null;
		}

		if (obj == null) {
			int size = this.size();
			if (size > 0) {
				logger.warn("读取出来的数据为空，但发现队列[" + this.getName() + "]里有[" + size + "]条尚未处理的数据，有可能是存在未提交的事物");
			}
		}

		return obj;
	}

	private Object lock = new byte[0];

	public void commit(Object data) throws Exception {

		if (currentDeliveryTag > 0) {
			synchronized (lock) {
				channel.basicAck(currentDeliveryTag, false);
				currentDeliveryTag = -1;
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.guttv.common.msg.queue.Consumer#size()
	 */
	@Override
	public int size() {
		try {
			return (int) channel.messageCount(name);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			return 0;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.guttv.common.msg.queue.Consumer#fallback(java.lang.Object)
	 */
	@Override
	public void fallback(Object data) throws Exception {
		if (currentDeliveryTag > 0) {
			synchronized (lock) {
				channel.basicReject(currentDeliveryTag, true);
				currentDeliveryTag = -1;
			}
		}
	}

	private Object data = null;

	public void reinit() {
		String addr = ConfigCache.getInstance().getProperty(Constants.RABBIT_ADDR, null);
		try {
			this.close();

			// 设置MabbitMQ所在主机ip或者主机名

			ConnectionFactory factory = new ConnectionFactory();

			factory.setUri(addr);

			connection = factory.newConnection();

			channel = connection.createChannel();

			channel.queueDeclareNoWait(name, this.durable, exclusive, autoDelete, null);
			channel.basicQos(1);

			if (StringUtils.isNotBlank(exchange)) {
				ExchangeType t = null;
				if (StringUtils.isBlank(type)) {
					t = ExchangeType.FANOUT;
				} else {
					t = Enum.valueOf(ExchangeType.class, type.toUpperCase());
				}
				channel.exchangeDeclare(exchange, t.getValue(), this.durable);

				channel.queueBind(name, exchange, this.routingKey);
			}

			consumer = new DefaultConsumer(channel) {
				int count = 0;

				@Override
				public void handleDelivery(String consumerTag, Envelope envelope, BasicProperties properties,
						byte[] body) throws IOException {

					if (currentDeliveryTag > 0) {
						logger.warn("发现有尚未提交的数据消息：" + currentDeliveryTag);
					}

					synchronized (lock) {
						currentDeliveryTag = envelope.getDeliveryTag();
						try {
							data = SerializeUtil.toObj(body);
						} catch (Exception e) {
							/*data = null;
							currentDeliveryTag = -1;

							logger.error("不能序列化的数据，将要被丢弃：" + new String(body, "UTF-8"));

							// 在这里应该回滚一下，或者提交一下，否则就死了，暂时决定提交一下
							channel.basicAck(envelope.getDeliveryTag(), false);

							logger.error(e.getMessage(), e);*/
							data = new String(body, "UTF-8");
						}
					}
					logger.debug("有您[" + name + "][" + consumerTag + "]的包裹[" + envelope.getDeliveryTag()
							+ "]，请注意查收，这里没敢打开看是什么");

					// 打印这个日志不太准确，最后一条只是最下来，尚未处理
					logger.info("[" + name + "]已经处理[" + (++count) + "]条数据");

					try {
						long size = channel.messageCount(name);
						if (size > 0) {
							logger.debug("还有[" + size + "]条数据等待处理");
						}

					} catch (Exception e1) {
						logger.error(e1.getMessage(), e1);
					}
				}
			};

			channel.basicConsume(name, false, consumer);

		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
	}

	private String exchange = "";
	private String type = ""; // exchenge的类型
	private String routingKey = "";
	private boolean durable = true;
	private boolean autoDelete = false;
	private boolean exclusive = false;

	/*
	 * public void reinit() { String addr =
	 * ConfigCache.getInstance().getProperty(Constants.RABBIT_ADDR, null);
	 * ConnectionFactory factory = new ConnectionFactory(); try { this.close();
	 * 
	 * factory.setUri(addr); // factory.setAutomaticRecoveryEnabled(true); //
	 * getting a connection connection = factory.newConnection();
	 * 
	 * // creating a channel channel = connection.createChannel(); // declaring
	 * a queue for this channel. If queue does not exist, // it will be created
	 * on the server. // 队列名称，是否持久化，是否独占，是否自动删除，其它参数 channel.queueDeclare(name,
	 * true, false, false, null);
	 * 
	 * // 保证一次只分发一个 该参数只能和basicConsumer的autoAck=false配合使用 // 需要程序调用
	 * channel.basicAck 方法 channel.basicQos(1);
	 * 
	 * consumer = new QueueingConsumer(channel); channel.basicConsume(name,
	 * false, consumer); } catch (Exception e) { logger.error(e.getMessage(),
	 * e); } }
	 */
}
