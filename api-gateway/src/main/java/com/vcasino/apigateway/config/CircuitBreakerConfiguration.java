package com.vcasino.apigateway.config;

import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.timelimiter.TimeLimiterConfig;
import org.springframework.cloud.circuitbreaker.resilience4j.ReactiveResilience4JCircuitBreakerFactory;
import org.springframework.cloud.circuitbreaker.resilience4j.Resilience4JConfigBuilder;
import org.springframework.cloud.client.circuitbreaker.Customizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
public class CircuitBreakerConfiguration {
    @Bean
    public Customizer<ReactiveResilience4JCircuitBreakerFactory> resilienceCustomizer() {
        return factory -> {
            factory.configureDefault(id -> new Resilience4JConfigBuilder(id)
                    .circuitBreakerConfig(createDefaultCircuitBreakerConfig())
                    .timeLimiterConfig(createTimeLimiterConfig(2))
                    .build());

            configure(factory, "user-service", 3);
            configure(factory, "wallet-service", 3);
            configure(factory, "clicker-service", 3);
            configure(factory, "notification-service", 3);
            configure(factory, "bet-service", 5);
        };
    }

    private void configure(ReactiveResilience4JCircuitBreakerFactory factory, String service, int timeoutSeconds) {
        factory.configure(builder -> builder
                        .circuitBreakerConfig(createDefaultCircuitBreakerConfig())
                        .timeLimiterConfig(createTimeLimiterConfig(timeoutSeconds)),
                service);
    }

    private CircuitBreakerConfig createDefaultCircuitBreakerConfig() {
        return CircuitBreakerConfig.custom()
                .slidingWindowSize(100)
                .minimumNumberOfCalls(50)
                .failureRateThreshold(50)
                .waitDurationInOpenState(Duration.ofSeconds(10))
                .permittedNumberOfCallsInHalfOpenState(10)
                .automaticTransitionFromOpenToHalfOpenEnabled(true)
                .build();
    }

    private TimeLimiterConfig createTimeLimiterConfig(int timeoutSeconds) {
        return TimeLimiterConfig.custom()
                .timeoutDuration(Duration.ofSeconds(timeoutSeconds))
                .build();
    }
}
