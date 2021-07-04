package com.yjl.assemblycappsystem.util;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.command.ActiveMQObjectMessage;
import org.apache.activemq.command.ActiveMQTextMessage;
import org.apache.activemq.pool.PooledConnectionFactory;
import org.springframework.beans.factory.annotation.Autowired;

import javax.jms.*;
import java.io.Serializable;

public class ActiveMQUtil {

    PooledConnectionFactory pooledConnectionFactory;


    public void init(String brokerURL){
        ActiveMQConnectionFactory factory = new ActiveMQConnectionFactory(brokerURL);
        //加入连接池
        PooledConnectionFactory pooledConnectionFactory = new PooledConnectionFactory(factory);
        //出现异常时重新连接
        pooledConnectionFactory.setReconnectOnException(true);
        pooledConnectionFactory.setMaxConnections(5);
        pooledConnectionFactory.setExpiryTimeout(10000);
        this.pooledConnectionFactory = pooledConnectionFactory;
    }

    public void sendText(Session session, String text, String queueName) throws JMSException {
        MessageProducer producer = getProducer(session,queueName);
        //初始化消息,使用字符串文本的形式
        /*写法一：ActiveMQTextMessage activeMQTextMessage = new ActiveMQTextMessage();
        activeMQTextMessage.setText(text);*/
        //写法二：
        TextMessage textMessage = session.createTextMessage(text);
        producer.send(textMessage);
        session.commit();
    }

    public void sentObject(Session session, String queueName, Serializable object) throws JMSException {
        MessageProducer producer = getProducer(session,queueName);
        ObjectMessage objectMessage = session.createObjectMessage(object);
        producer.send(objectMessage);
        session.commit();
    }

    private MessageProducer getProducer(Session session,String queueName) throws JMSException {
        //创建Queue发送消息
        Queue parseModuleQueue =  session.createQueue(queueName);
        //创建queue消息的生产者
        MessageProducer producer = session.createProducer(parseModuleQueue);
        return producer;
    }


    public Connection getConnection() throws JMSException {
        // 从连接池中获取PooledConnection和PooledSession的方式
        // 1.直接调用池化连接工厂中的创建连接方法==>获取到的就是池化的连接
        Connection connection = pooledConnectionFactory.createConnection();
        return connection;
    }


}
