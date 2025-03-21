package com.vcasino.clicker;

import jakarta.annotation.PostConstruct;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

import java.util.TimeZone;

@SpringBootApplication
@EnableDiscoveryClient
public class ClickerApplication {

    public static void main(String[] args) {
        SpringApplication.run(ClickerApplication.class, args);
    }

    @PostConstruct
    public void setApplicationTimezone() {
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
    }
}
