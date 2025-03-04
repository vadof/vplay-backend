package com.vcasino.user.config.kafka;

import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.common.config.TopicConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfig {

    @Bean
    public NewTopic userTopic() {
        return TopicBuilder
                .name(Topic.USER_CREATE.getName())
                .replicas(1)
                .partitions(3)
                .config(TopicConfig.RETENTION_MS_CONFIG, "604800000") // 7 Days
                .config(TopicConfig.CLEANUP_POLICY_CONFIG, TopicConfig.CLEANUP_POLICY_DELETE)
                .build();
    }

    @Bean
    public NewTopic clickTopic() {
        return TopicBuilder
                .name(Topic.CLICK_EVENTS.getName())
                .replicas(1)
                .partitions(5)
                .config(TopicConfig.RETENTION_MS_CONFIG, "86400000") // 1 Day
                .config(TopicConfig.CLEANUP_POLICY_CONFIG, TopicConfig.CLEANUP_POLICY_DELETE)
                .build();
    }

}
