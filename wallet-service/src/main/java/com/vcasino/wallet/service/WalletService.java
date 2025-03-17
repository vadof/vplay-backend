package com.vcasino.wallet.service;

import com.vcasino.common.kafka.Topic;
import com.vcasino.common.kafka.event.CurrencyConversionEvent;
import com.vcasino.common.kafka.event.CurrencyConversionPayload;
import com.vcasino.common.enums.Currency;
import com.vcasino.wallet.entity.ProcessedEvent;
import com.vcasino.wallet.entity.Wallet;
import com.vcasino.wallet.repository.ProcessedEventRepository;
import com.vcasino.wallet.repository.WalletRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.UUID;

@Service
@AllArgsConstructor
@Slf4j
public class WalletService {

    private final WalletRepository walletRepository;
    private final ProcessedEventRepository processedEventRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Transactional
    public void createWallet(Long id) {
        Wallet wallet = Wallet.builder()
                .id(id)
                .balance(BigDecimal.ZERO)
                .updatedAt(Instant.now())
                .frozen(false)
                .version(0)
                .build();

        walletRepository.save(wallet);
        log.info("Wallet#{} saved to database", id);
    }

    public Wallet getById(Long id) {
        return walletRepository.findById(id).orElseThrow(
                () -> {
                    String message = "Wallet#" + id + " not found";
                    log.error(message);
                    return new RuntimeException(message);
                });
    }

    @Transactional
    public void convertCurrency(CurrencyConversionEvent currencyConversion) {
        if (processedEventRepository.existsById(currencyConversion.eventId())) {
            log.warn("Duplicate event detected: {}", currencyConversion.eventId());
            sendProcessedEvent(currencyConversion.eventId());
            return;
        }

        Wallet wallet = getById(currencyConversion.aggregateId());

        CurrencyConversionPayload payload = currencyConversion.payload();
        if (payload.from().equals(Currency.VCOIN) && payload.to().equals(Currency.VDOLLAR)) {
            convertVCoinsToVDollars(wallet, payload.amount());
        } else {
            log.error("Incorrect conversion currencies");
            return;
        }

        wallet.setUpdatedAt(Instant.now());
        walletRepository.save(wallet);

        ProcessedEvent processedEvent = new ProcessedEvent(currencyConversion.eventId(), Instant.now());
        processedEventRepository.save(processedEvent);

        log.info("Event - '{}' processed", processedEvent.getEventId());

        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                // TODO send notification
                sendProcessedEvent(processedEvent.getEventId());
            }
        });
    }

    private void convertVCoinsToVDollars(Wallet wallet, BigDecimal amount) {
        BigDecimal vDollars = amount.divide(new BigDecimal("100000"), 2, RoundingMode.DOWN);
        wallet.setBalance(wallet.getBalance().add(vDollars));
    }

    private void sendProcessedEvent(UUID eventId) {
        kafkaTemplate.send(Topic.PROCESSED_EVENTS.getName(), new com.vcasino.common.kafka.event.ProcessedEvent(eventId));
    }

}
