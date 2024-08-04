package com.vcasino.clicker.configuration;

import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Configuration;

import java.util.TimeZone;

@Configuration
public class ApplicationConfig {

    @PostConstruct
    public void setApplicationTimezone() {
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
    }
}
