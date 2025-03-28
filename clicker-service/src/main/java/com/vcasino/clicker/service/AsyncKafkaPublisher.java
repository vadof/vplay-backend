package com.vcasino.clicker.service;

import com.vcasino.commonkafka.enums.Topic;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class AsyncKafkaPublisher {

    private final KafkaTemplate<String, Object> retryKafkaTemplate;
    private final KafkaTemplate<String, Object> atMostOnceKafkaTemplate;

    @Autowired
    public AsyncKafkaPublisher(@Qualifier("defaultRetryTopicKafkaTemplate") KafkaTemplate<String, Object> retryKafkaTemplate,
                               @Qualifier("atMostOnceKafkaTemplate") KafkaTemplate<String, Object> atMostOnceKafkaTemplate) {
        this.retryKafkaTemplate = retryKafkaTemplate;
        this.atMostOnceKafkaTemplate = atMostOnceKafkaTemplate;
    }

    @Async
    public void send(Topic topic, Object o, boolean retryable) {
        if (retryable) {
            retryKafkaTemplate.send(topic.getName(), o);
        } else {
            atMostOnceKafkaTemplate.send(topic.getName(), o);
        }
    }
}
