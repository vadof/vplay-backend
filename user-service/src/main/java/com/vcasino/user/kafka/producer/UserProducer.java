package com.vcasino.user.kafka.producer;

import com.vcasino.user.config.kafka.Topic;
import com.vcasino.user.kafka.message.UserCreate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class UserProducer extends AbstractProducer {

    public UserProducer(KafkaTemplate<String, Object> kafkaTemplate) {
        super(kafkaTemplate);
    }

    public void sendUserCreated(Long userId) {
        log.info("Send user-create event - User#{}", userId);
        send(Topic.USER_CREATE.getName(), new UserCreate(userId));
    }

}
