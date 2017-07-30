package com.date;

import javax.annotation.Resource;

import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.retry.backoff.Sleeper;

import com.date.producer.MessageProducer;


/**
 * Created by amy on 17-7-30.
 */

public class MessageTest {

    private Logger logger = LoggerFactory.getLogger(MessageTest.class);

    private ApplicationContext context = null;

    @Before
    public void setUp() {
        context = new ClassPathXmlApplicationContext("applicationContext.xml");
    }

    @Test
    public void should_send_a_amq_message() throws Exception {
        MessageProducer messageProducer = (MessageProducer) context.getBean("messageProducer");
        int a = Integer.MAX_VALUE;
        while (a > 0) {
            messageProducer.sendMessage("Hello, I am amq sender num :" + a--);
            try {
                //暂停一下，好让消息消费者去取消息打印出来
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }
    }
}