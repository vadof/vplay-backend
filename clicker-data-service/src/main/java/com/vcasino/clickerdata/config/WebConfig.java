package com.vcasino.clickerdata.config;

import com.vcasino.clickerdata.dto.ChartOption;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addFormatters(FormatterRegistry registry) {
        registry.addConverter(chartOptionConverter());
    }

    @Bean
    public Converter<String, ChartOption> chartOptionConverter() {
        return new Converter<String, ChartOption>() {
            @Override
            public ChartOption convert(String source) {
                return ChartOption.fromString(source);
            }
        };
    }

}
