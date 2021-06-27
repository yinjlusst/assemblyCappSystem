package com.yjl.asemblycappsystem.util;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

public class RedisUtil {


    private JedisPool jedisPool;

    /**
     * 初始化并建立一个连接池
     *
     * @param host
     * @param port
     * @param database
     */
    public void initPool(String host, int port, int database) {
        JedisPoolConfig poolConfig = new JedisPoolConfig();
        poolConfig.setMaxTotal(200);//设置最大连接数量
        poolConfig.setMaxIdle(30);
        poolConfig.setBlockWhenExhausted(true);
        poolConfig.setMaxWaitMillis(10 * 1000);
        poolConfig.setTestOnBorrow(true);
        jedisPool = new JedisPool(poolConfig, host, port, 20 * 1000);
    }

    /**
     * 从连接池中取出一个连接
     *
     * @return
     */
    public Jedis getJedis() {
        Jedis jedis = jedisPool.getResource();
        return jedis;
    }
}
