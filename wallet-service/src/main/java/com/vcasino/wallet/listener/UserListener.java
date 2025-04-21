package com.vcasino.wallet.listener;

import com.vcasino.commonkafka.event.UserCreateEvent;
import com.vcasino.wallet.service.WalletService;
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

    private final WalletService walletService;

    @RetryableTopic(backoff = @Backoff(value = 2000))
    @KafkaListener(
            groupId = "wallet-service-group",
            topics = "user-create",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void handle(UserCreateEvent userData, Acknowledgment ack) {
        log.info("Received user-create event - {}", userData);
        walletService.createWallet(userData.id(), userData.invitedByUserId());
        ack.acknowledge();
    }
}
