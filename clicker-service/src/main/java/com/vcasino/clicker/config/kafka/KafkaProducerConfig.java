package com.vcasino.clicker.config.kafka;

import com.vcasino.common.kafka.config.ProducerConfigs;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaTemplate;

@Configuration
public class KafkaProducerConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Bean(name = "defaultRetryTopicKafkaTemplate")
    public KafkaTemplate<String, Object> kafkaTemplate() {
        String id = "clicker-service-producer-0";
        return ProducerConfigs.buildAtLeastOnceKafkaTemplate(id, bootstrapServers);
    }

    @Bean(name = "atMostOnceKafkaTemplate")
    public KafkaTemplate<String, Object> atMostOnceKafkaTemplate() {
        String id = "clicker-service-producer-1";
        return ProducerConfigs.buildAtMostOnceKafkaTemplate(id, bootstrapServers);
    }
}
