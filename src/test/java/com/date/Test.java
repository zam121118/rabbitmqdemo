package com.date;

import com.date.consumer.QueueConsumer;
import com.date.producer.Producer;

import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;

/**
 * 用途： 测试本机rabbitmq-server正常
 * Created by amy on 17-7-29.
 */
public class Test {

    public Test() throws Exception{

        QueueConsumer consumer = new QueueConsumer("queue");
        Thread consumerThread = new Thread(consumer);
        consumerThread.start();

        Producer producer = new Producer("queue");

        for (int i = 0; i < 1000000; i++) {
            HashMap message = new HashMap();
            message.put("message number", i);
            producer.sendMessage(message);
            System.out.println("Message Number "+ i +" sent.");
        }
    }

    /**
     * @param args
     * @throws SQLException
     * @throws IOException
     */
    public static void main(String[] args) throws Exception{
        new Test();
    }
}