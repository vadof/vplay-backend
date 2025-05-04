package com.vcasino.bet.config.redis;

import com.vcasino.bet.config.ApplicationConfig;
import com.vcasino.bet.service.MatchWebsocketService;
import com.vcasino.commonredis.config.JedisConfig;
import com.vcasino.commonredis.enums.Channel;
import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;

@Configuration
@AllArgsConstructor
public class RedisConfig {

    private final MatchWebsocketService matchWebsocketService;
    private final ApplicationConfig config;

    @Bean
    public JedisConnectionFactory jedisConnectionFactory() {
        return JedisConfig.buildJedisConnectionFactory(config.getRedis().getHostName(),
                config.getRedis().getPort(), config.getRedis().getPassword());
    }

    @Bean
    public RedisMessageListenerContainer redisMessageListenerContainer(JedisConnectionFactory connectionFactory) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);

        MessageListenerAdapter matchListenerAdapter = new MessageListenerAdapter(matchWebsocketService, "onMessage");
        container.addMessageListener(matchListenerAdapter, new ChannelTopic(Channel.BET_MATCH.getName()));
        container.addMessageListener(matchListenerAdapter, new ChannelTopic(Channel.BET_MATCH_MARKETS.getName()));

        return container;
    }

    @Bean
    public StringRedisTemplate redisTemplate(JedisConnectionFactory jedisConnectionFactory) {
        return new StringRedisTemplate(jedisConnectionFactory);
    }


}
