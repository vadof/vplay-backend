package com.vcasino.wallet;

import jakarta.annotation.PostConstruct;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.TimeZone;

@SpringBootApplication
public class WalletServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(WalletServiceApplication.class, args);
    }

    @PostConstruct
    public void setApplicationTimezone() {
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
    }

}
