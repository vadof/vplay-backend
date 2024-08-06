package com.vcasino.user.kafka.producer;

import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;

@AllArgsConstructor
public abstract class AbstractProducer {

    protected final Logger log = LoggerFactory.getLogger(this.getClass());

    private final KafkaTemplate<String, Object> kafkaTemplate;

    protected void send(String topic, Object data) {
        kafkaTemplate.send(topic, data);
    }

}
