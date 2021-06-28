package com.yjl.assemblycappsystem.util;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.command.ActiveMQTextMessage;
import org.apache.activemq.pool.PooledConnectionFactory;

import javax.jms.*;

public class ActiveMQUtil {

    PooledConnectionFactory pooledConnectionFactory = null;

    public ConnectionFactory init(String brokerUrl){
        ActiveMQConnectionFactory factory = new ActiveMQConnectionFactory();
        //加入连接池
        pooledConnectionFactory = new PooledConnectionFactory(factory);
        //出现异常时重新连接
        pooledConnectionFactory.setReconnectOnException(true);
        pooledConnectionFactory.setMaxConnections(5);
        pooledConnectionFactory.setExpiryTimeout(10000);
        return pooledConnectionFactory;
    }

    public ConnectionFactory getConnectionFactory(){
        return this.pooledConnectionFactory;
    }

    public void sendText(Session session, String text, String queueName) throws JMSException {
        //创建Queue发送消息
        Queue parseModuleQueue =  session.createQueue(queueName);
        //创建queue消息的生产者
        MessageProducer producer = session.createProducer(parseModuleQueue);
        //初始化消息,使用字符串文本的形式
        ActiveMQTextMessage activeMQTextMessage = new ActiveMQTextMessage();
        activeMQTextMessage.setText(text);
        producer.send(activeMQTextMessage);
        session.commit();
    }
}
