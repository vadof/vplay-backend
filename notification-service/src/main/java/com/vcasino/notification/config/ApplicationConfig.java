package com.vcasino.notification.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Setter
@Getter
@Component
@ConfigurationProperties(prefix = "config")
public class ApplicationConfig {

    private String clientUrl;
    private JwtProperties jwt;
    private Redis redis;

    @Setter
    @Getter
    public static class JwtProperties {
        private String publicKeyPath;
    }

    @Setter
    @Getter
    public static class Redis {
        private String hostName;
        private Integer port;
        private String password;
    }
}
