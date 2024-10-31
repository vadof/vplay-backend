package com.vcasino.clicker.config;

import com.vcasino.clicker.entity.enums.RewardType;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addFormatters(FormatterRegistry registry) {
        registry.addConverter(integratedServiceConverter());
        registry.addConverter(rewardTypeConverter());
    }

    @Bean
    public Converter<String, IntegratedService> integratedServiceConverter() {
        return new Converter<String, IntegratedService>() {
            @Override
            public IntegratedService convert(String source) {
                return IntegratedService.fromValue(source);
            }
        };
    }

    @Bean
    public Converter<String, RewardType> rewardTypeConverter() {
        return new Converter<String, RewardType>() {
            @Override
            public RewardType convert(String source) {
                return RewardType.fromValue(source);
            }
        };
    }

}
