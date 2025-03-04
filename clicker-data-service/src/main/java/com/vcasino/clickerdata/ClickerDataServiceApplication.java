package com.vcasino.clickerdata;

import jakarta.annotation.PostConstruct;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.TimeZone;

@SpringBootApplication
@EnableScheduling
public class ClickerDataServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(ClickerDataServiceApplication.class, args);
    }

    @PostConstruct
    public void setApplicationTimezone() {
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
    }

}
