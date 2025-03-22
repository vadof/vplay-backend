package com.vcasino.wallet.service;

import com.vcasino.wallet.entity.OutboxEvent;
import com.vcasino.wallet.entity.Wallet;
import com.vcasino.wallet.entity.enums.EventStatus;
import com.vcasino.wallet.entity.enums.EventType;
import com.vcasino.wallet.repository.OutboxEventRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
@AllArgsConstructor
@Slf4j
public class EventFinisherService {

    private final OutboxEventRepository outboxEventRepository;
    private final WalletService walletService;
    private final CurrencyConversionService currencyService;

    public OutboxEvent getOutboxEventById(UUID id) {
        return outboxEventRepository.findById(id)
                .orElseThrow(() -> {
                    String message = "OutboxEvent#" + id + " not found";
                    log.error(message);
                    return new RuntimeException(message);
                });
    }

    @Transactional
    public void completeEvent(UUID eventId) {
        OutboxEvent outboxEvent = getOutboxEventById(eventId);
        completeEvent(outboxEvent);
    }

    @Transactional
    public void completeEvent(OutboxEvent outboxEvent) {
        if (outboxEvent.getStatus().equals(EventStatus.COMPLETED)) return;

        Wallet wallet = null;
        if (outboxEvent.getType().equals(EventType.CURRENCY_CONVERSION)) {
            wallet = currencyService.completeCurrencyConversion(outboxEvent);
        }

        outboxEvent.setStatus(EventStatus.COMPLETED);
        outboxEvent.setModifiedAt(Instant.now());

        if (wallet != null) {
            walletService.save(wallet);
        }

        outboxEventRepository.save(outboxEvent);

        log.info("Event {} completed", outboxEvent.getId());
    }

    @Transactional
    public void cancelEvent(UUID eventId) {
        OutboxEvent outboxEvent = getOutboxEventById(eventId);
        cancelEvent(outboxEvent);
    }

    @Transactional
    public void cancelEvent(OutboxEvent outboxEvent) {
        if (outboxEvent.getStatus().equals(EventStatus.COMPLETED)) return;

        Wallet wallet = null;
        if (outboxEvent.getType().equals(EventType.CURRENCY_CONVERSION)) {
            wallet = currencyService.cancelCurrencyConversion(outboxEvent);
        }

        outboxEvent.setStatus(EventStatus.COMPLETED);
        outboxEvent.setModifiedAt(Instant.now());

        if (wallet != null) {
            walletService.save(wallet);
        }

        outboxEventRepository.save(outboxEvent);

        log.info("Event {} cancelled", outboxEvent.getId());
    }

}
