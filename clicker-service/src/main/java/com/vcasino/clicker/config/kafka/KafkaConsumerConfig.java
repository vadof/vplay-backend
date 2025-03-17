package com.vcasino.clicker.config.kafka;

import com.vcasino.common.kafka.config.ConsumerConfigs;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.KafkaListenerContainerFactory;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;

@Configuration
public class KafkaConsumerConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Bean
    public KafkaListenerContainerFactory<
            ConcurrentMessageListenerContainer<String, Object>> kafkaListenerContainerFactory() {
        return ConsumerConfigs.buildKafkaListenerContainerFactory("clicker-service-group", bootstrapServers);
    }

}
