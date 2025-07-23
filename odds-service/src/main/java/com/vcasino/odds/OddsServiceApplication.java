package com.vcasino.odds;

import jakarta.annotation.PostConstruct;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.TimeZone;

@SpringBootApplication
@EnableDiscoveryClient
@EnableScheduling
@EnableAsync
public class OddsServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(OddsServiceApplication.class, args);
    }

    @PostConstruct
    void setSystemProperties() {
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
    }

}
