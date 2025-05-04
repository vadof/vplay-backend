package com.vcasino.notification.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vcasino.commonredis.config.JedisConfig;
import com.vcasino.commonredis.enums.Channel;
import com.vcasino.notification.listener.RedisNotificationListener;
import com.vcasino.notification.service.WebSocketService;
import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;

@Configuration
@AllArgsConstructor
public class RedisConfig {

    private final ApplicationConfig config;

    @Bean
    public JedisConnectionFactory jedisConnectionFactory() {
        return JedisConfig.buildJedisConnectionFactory(config.getRedis().getHostName(),
                config.getRedis().getPort(), config.getRedis().getPassword());
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
    public RedisNotificationListener redisNotificationListener(WebSocketService webSocketService, ObjectMapper objectMapper) {
        return new RedisNotificationListener(webSocketService, objectMapper);
    }

    @Bean
    public StringRedisTemplate redisTemplate(JedisConnectionFactory jedisConnectionFactory) {
        return new StringRedisTemplate(jedisConnectionFactory);
    }

}
