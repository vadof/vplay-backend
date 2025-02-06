package com.vcasino.user.kafka.producer;

import com.vcasino.user.config.kafka.Topic;
import com.vcasino.user.kafka.message.UserCreate;
import jakarta.annotation.Nullable;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class UserProducer extends AbstractProducer {

    public UserProducer(KafkaTemplate<String, Object> kafkaTemplate) {
        super(kafkaTemplate);
    }

    public void sendUserCreated(Long id, String username, @Nullable String invitedBy) {
        log.info("Send user-create event for \"{}\"", username);
        send(Topic.USER_CREATE.getName(), new UserCreate(id, username, invitedBy));
    }

}
