package com.vcasino.wallet.listener;

import com.vcasino.common.kafka.event.CurrencyConversionEvent;
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
public class CurrencyConversionListener {

    private final WalletService walletService;

    @RetryableTopic(backoff = @Backoff(value = 2000))
    @KafkaListener(
            groupId = "wallet-service-group",
            topics = "currency-conversion",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void handle(CurrencyConversionEvent event, Acknowledgment ack) {
        log.info("Received currency conversion event - {}", event);
        walletService.convertCurrency(event);
        ack.acknowledge();
    }
}
