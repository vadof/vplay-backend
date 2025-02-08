package com.vcasino.clicker.config;

import com.vcasino.clicker.entity.enums.TaskType;
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
        registry.addConverter(taskTypeConverter());
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
    public Converter<String, TaskType> taskTypeConverter() {
        return new Converter<String, TaskType>() {
            @Override
            public TaskType convert(String source) {
                return TaskType.fromValue(source);
            }
        };
    }

}
