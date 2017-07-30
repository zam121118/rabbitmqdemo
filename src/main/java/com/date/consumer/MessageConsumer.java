package com.date.consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageListener;

import javax.xml.ws.handler.MessageContext;


/**
 * 功能概要：消费接收
 * Created by amy on 17-7-29.
 */
public class MessageConsumer implements MessageListener{

    private Logger logger = LoggerFactory.getLogger(MessageConsumer.class);

    @Override
    public void onMessage(Message message) {
        logger.info("receive message:{}",message);
    }

}