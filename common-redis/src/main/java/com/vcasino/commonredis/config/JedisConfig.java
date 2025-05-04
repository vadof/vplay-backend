package com.vcasino.commonredis.config;

import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.jedis.JedisClientConfiguration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import redis.clients.jedis.JedisPoolConfig;

import java.time.Duration;

public class JedisConfig {

    public static JedisConnectionFactory buildJedisConnectionFactory(String hostName, Integer port, String password) {
        JedisPoolConfig poolConfig = new JedisPoolConfig();
        poolConfig.setMaxTotal(50);
        poolConfig.setMaxIdle(10);
        poolConfig.setMinIdle(5);
        poolConfig.setMaxWait(Duration.ofSeconds(3));
        poolConfig.setTestOnBorrow(true);

        JedisClientConfiguration clientConfig = JedisClientConfiguration.builder()
                .connectTimeout(Duration.ofSeconds(2))
                .usePooling()
                .build();

        RedisStandaloneConfiguration redisConfig = new RedisStandaloneConfiguration(hostName, port);
        redisConfig.setPassword(password);

        return new JedisConnectionFactory(redisConfig, clientConfig);
    }

}
