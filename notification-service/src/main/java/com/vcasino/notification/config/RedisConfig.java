package com.vcasino.notification.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vcasino.commonredis.config.JedisConfig;
import com.vcasino.commonredis.enums.Channel;
import com.vcasino.notification.listener.RedisNotificationListener;
import com.vcasino.notification.service.SseEmitterService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;

@Configuration
public class RedisConfig {

    @Bean
    public JedisConnectionFactory jedisConnectionFactory() {
        return JedisConfig.buildJedisConnectionFactory();
    }

    @Bean
    public RedisMessageListenerContainer container(
            JedisConnectionFactory connectionFactory,
            RedisNotificationListener listener) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        container.addMessageListener(listener, new ChannelTopic(Channel.NOTIFICATIONS.getName()));
        return container;
    }

    @Bean
    public RedisNotificationListener redisNotificationListener(SseEmitterService emitterService, ObjectMapper objectMapper) {
        return new RedisNotificationListener(emitterService, objectMapper);
    }

    @Bean
    public StringRedisTemplate redisTemplate(JedisConnectionFactory jedisConnectionFactory) {
        return new StringRedisTemplate(jedisConnectionFactory);
    }

}
