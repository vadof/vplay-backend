package com.vcasino.clicker.listener;

import com.vcasino.clicker.service.AccountService;
import com.vcasino.common.kafka.event.UserCreateEvent;
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

    private final AccountService accountService;

    @RetryableTopic(backoff = @Backoff(value = 2000))
    @KafkaListener(
            groupId = "clicker-service-group",
            topics = "user-create",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void handle(UserCreateEvent userData, Acknowledgment ack) {
        log.info("Received user-create event - {}", userData);
        accountService.createAccount(userData.id(), userData.username(), userData.invitedBy());
        ack.acknowledge();
    }
}
