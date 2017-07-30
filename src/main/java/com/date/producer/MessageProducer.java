package com.date.producer;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.stereotype.Service;


/**
 * 功能概要： 消息生产，提交到队列中
 * Created by amy on 17-7-29.
 */

@Service
public class MessageProducer {

    private Logger logger = LoggerFactory.getLogger(MessageProducer.class);

    @Resource
    private AmqpTemplate amqpTemplate;

    public void sendMessage(Object message){
        logger.info("to send message:{}",message);
        amqpTemplate.convertAndSend("queueTestKey",message);
    }
}