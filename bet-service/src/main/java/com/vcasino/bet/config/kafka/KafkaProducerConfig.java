package com.vcasino.bet.config.kafka;

import com.vcasino.commonkafka.config.ProducerConfigs;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaTemplate;

@Configuration
public class KafkaProducerConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Bean
    public KafkaTemplate<String, Object> kafkaTemplate() {
        return ProducerConfigs.buildAtLeastOnceKafkaTemplate("bet-service-producer", bootstrapServers);
    }

}
