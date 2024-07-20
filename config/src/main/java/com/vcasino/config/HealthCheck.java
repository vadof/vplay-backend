package com.vcasino.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.actuate.health.AbstractHealthIndicator;
import org.springframework.boot.actuate.health.Health;
import org.springframework.stereotype.Component;

@Component
public class HealthCheck extends AbstractHealthIndicator {

    private static final Logger log = LoggerFactory.getLogger(HealthCheck.class);

    @Override
    protected void doHealthCheck(Health.Builder builder) throws Exception {
        log.info("Received HealthCheck request");
        builder.up().withDetail("status", "UP");
    }
}
