package com.guttv.pm.frame.msg.rabbit;

import com.guttv.pm.core.msg.ConsumerFactory;
import com.guttv.pm.core.msg.rabbit.RabbitConsumer;

public class RabbitTopicConsumerForQueue {

	public static void main(String[] args) throws Exception {

		String path = "rabbit://test_123456";
		RabbitConsumer consumer = (RabbitConsumer) ConsumerFactory.create(path);
		while (true) {
			
			Object message = consumer.read();
			if (message != null) {
				System.out.println(consumer.getName() + ":收到数据,还有[" + consumer.size() + "]没有处理：" + message);
				consumer.commit(null);
				Thread.sleep(1000);
			}
		}
	}
}
