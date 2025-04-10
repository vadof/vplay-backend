package com.vcasino.bet.listener;

import com.vcasino.bet.service.UserService;
import com.vcasino.commonkafka.event.UserCreateEvent;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.retry.annotation.Backoff;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
@Slf4j
public class UserListener {

    private final UserService userService;

    @RetryableTopic(backoff = @Backoff(value = 2000))
    @KafkaListener(
            groupId = "bet-service-group",
            topics = "user-create",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void handle(UserCreateEvent userData, Acknowledgment ack) {
        log.info("Received user-create event - {}", userData);
        userService.createUser(userData.id());
        ack.acknowledge();
    }
}
