package com.date.producer;

import java.io.IOException;
import java.io.Serializable;
import java.util.concurrent.TimeoutException;

import com.date.EndPoint;
import org.apache.commons.lang.SerializationUtils;


/**
 * 功能概要： 消息生产者
 * 用途： 测试本机rabbitmq-server正常
 * Created by amy on 17-7-29.
 */
public class Producer extends EndPoint {

    public Producer(String endPointName) throws IOException, TimeoutException {
        super(endPointName);
    }

    public void sendMessage(Serializable object) throws IOException {
        channel.basicPublish("",endPointName, null, SerializationUtils.serialize(object));
    }
}