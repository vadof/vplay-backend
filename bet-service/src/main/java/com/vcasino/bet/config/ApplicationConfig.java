package com.vcasino.bet.config;

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
    private String clientUrl;
    private Redis redis;

    private S3 s3;

    @Setter
    @Getter
    public static class S3 {
        private String bucket;
        private String endpoint;
        private String region;
        private String accessKey;
        private String secretKey;
    }

    @Setter
    @Getter
    public static class Redis {
        private String hostName;
        private Integer port;
        private String password;
    }

}
