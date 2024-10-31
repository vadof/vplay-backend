package com.vcasino.clicker.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Getter
public class ApiKeys {

    @Value("${youtube.api-key}")
    private String youtubeApiKey;

}
