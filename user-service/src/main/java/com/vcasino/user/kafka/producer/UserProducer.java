package com.vcasino.user.kafka.producer;

import com.vcasino.user.kafka.message.UserCreated;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class UserProducer extends AbstractProducer {

    public UserProducer(KafkaTemplate<String, Object> kafkaTemplate) {
        super(kafkaTemplate);
    }

    public void sendUserCreated(Long userId) {
        log.info("Send User#{} created", userId);
        send("user-topic", new UserCreated(userId));
    }

}
