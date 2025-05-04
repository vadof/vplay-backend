package com.vcasino.wallet.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Setter
@Getter
@Component
@ConfigurationProperties(prefix = "config")
public class ApplicationConfig {

    private Redis redis;

    @Setter
    @Getter
    public static class Redis {
        private String hostName;
        private Integer port;
        private String password;
    }

}
