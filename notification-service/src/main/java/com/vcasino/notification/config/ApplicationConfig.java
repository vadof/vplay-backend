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

    @Setter
    @Getter
    public static class JwtProperties {
        private String publicKeyPath;
    }
}
