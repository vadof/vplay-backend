package com.vcasino.clicker.kafka.listener;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vcasino.clicker.kafka.message.UserCreate;
import com.vcasino.clicker.service.AccountService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.retry.annotation.Backoff;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class UserListener extends AbstractListener {

    private final AccountService accountService;

    @Autowired
    public UserListener(ObjectMapper mapper, AccountService accountService) {
        super(mapper);
        this.accountService = accountService;
    }

    @RetryableTopic(backoff = @Backoff(value = 2000))
    @KafkaListener(id = "clicker-service-group", topics = "user-create")
    public void handle(String data, Acknowledgment ack) {
        log.info("Received user-create event - {}", data);
        UserCreate userData = fromJson(data, UserCreate.class);
        accountService.createAccount(userData.id(), userData.username(), userData.invitedBy());
        ack.acknowledge();
    }
}
