package com.vcasino.bet.config.redis;

import com.vcasino.bet.config.ApplicationConfig;
import lombok.AllArgsConstructor;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@AllArgsConstructor
public class RedissonConfig {

    private final ApplicationConfig appConfig;

    @Bean
    public RedissonClient redissonClient() {
        Config config = new Config();
        String address = "redis://" + appConfig.getRedis().getHostName() + ":" + appConfig.getRedis().getPort();
        config.useSingleServer()
                .setAddress(address)
                .setPassword(appConfig.getRedis().getPassword())
                .setConnectionMinimumIdleSize(2);
        return Redisson.create(config);
    }
}