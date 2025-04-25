package com.vcasino.bet;

import jakarta.annotation.PostConstruct;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.TimeZone;

@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients
@EnableScheduling
public class BetServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(BetServiceApplication.class, args);
    }

    @PostConstruct
    public void setApplicationTimezone() {
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
    }

}
