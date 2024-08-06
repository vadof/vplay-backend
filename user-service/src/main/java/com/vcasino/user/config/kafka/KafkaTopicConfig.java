package com.vcasino.user.config.kafka;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfig {

    @Bean
    public NewTopic userTopic() {
        return TopicBuilder
                .name("user-topic")
                .replicas(1)
                .partitions(1)
                .build();
    }

}
