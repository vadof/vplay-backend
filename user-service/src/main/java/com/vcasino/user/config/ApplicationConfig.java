package com.vcasino.user.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Setter
@Getter
@Component
@ConfigurationProperties(prefix = "config")
public class ApplicationConfig {

    private Boolean production;
    private String adminPassword;
    private JwtProperties jwt;
    private String clientUrl;
    private ConfirmationProperties confirmation;
    private String smtpSender;

    @Setter
    @Getter
    public static class JwtProperties {
        private long expirationMs;
        private long refreshExpirationMs;
        private String keysPath;
    }

    @Setter
    @Getter
    public static class ConfirmationProperties {
        private TokenProperties token;

        @Setter
        @Getter
        public static class TokenProperties {
            private long expirationMs;
        }
    }
}

