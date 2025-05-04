package com.vcasino.wallet.config;

import com.vcasino.commonredis.config.JedisConfig;
import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;

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
    public StringRedisTemplate redisTemplate(JedisConnectionFactory jedisConnectionFactory) {
        return new StringRedisTemplate(jedisConnectionFactory);
    }

}
