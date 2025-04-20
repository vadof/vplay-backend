package com.vcasino.wallet.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vcasino.commonredis.enums.Channel;
import com.vcasino.commonredis.enums.NotificationType;
import com.vcasino.commonredis.event.NotificationEvent;
import com.vcasino.wallet.client.EventCreatedResponse;
import com.vcasino.wallet.client.ReservationRequest;
import com.vcasino.wallet.client.ReservationType;
import com.vcasino.wallet.entity.OutboxEvent;
import com.vcasino.wallet.entity.CurrencyReservationPayload;
import com.vcasino.wallet.entity.Wallet;
import com.vcasino.wallet.entity.enums.Applicant;
import com.vcasino.wallet.entity.enums.EventStatus;
import com.vcasino.wallet.entity.enums.EventType;
import com.vcasino.wallet.exception.AppException;
import com.vcasino.wallet.exception.CurrencyConversionException;
import com.vcasino.wallet.repository.OutboxEventRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.math.BigDecimal;
import java.time.Instant;

@Service
@AllArgsConstructor
@Slf4j
public class CurrencyService {

    private final WalletService walletService;
    private final ObjectMapper objectMapper;
    private final OutboxEventRepository outboxEventRepository;
    private final StringRedisTemplate redisTemplate;

    @Transactional
    public EventCreatedResponse reserveCurrency(ReservationRequest request) {
        if (request.getType().equals(ReservationType.WITHDRAWAL)) {
            return withdrawCurrency(request);
        } else {
            return depositCurrency(request);
        }
    }

    private EventCreatedResponse withdrawCurrency(ReservationRequest request) {
        Wallet wallet = walletService.getById(request.getAggregateId());
        BigDecimal amount = request.getAmount();

        validateBalance(wallet, amount);

        wallet.setBalance(wallet.getBalance().subtract(amount));
        wallet.setReserved(wallet.getReserved().add(amount));

        CurrencyReservationPayload payload =
                new CurrencyReservationPayload(request.getType(), request.getAmount());

        OutboxEvent outboxEvent = createCurrencyReservationEvent(wallet.getId(), payload, request.getApplicant());

        wallet = walletService.save(wallet);
        outboxEvent = outboxEventRepository.save(outboxEvent);

        return new EventCreatedResponse(outboxEvent.getId(), wallet.getBalance());
    }

    private EventCreatedResponse depositCurrency(ReservationRequest request) {
        Wallet wallet = walletService.getById(request.getAggregateId());

        CurrencyReservationPayload payload =
                new CurrencyReservationPayload(request.getType(), request.getAmount());

        OutboxEvent outboxEvent = createCurrencyReservationEvent(wallet.getId(), payload, request.getApplicant());

        outboxEvent = outboxEventRepository.save(outboxEvent);

        return new EventCreatedResponse(outboxEvent.getId(), wallet.getBalance());
    }

    private void validateBalance(Wallet wallet, BigDecimal amount) {
        if (wallet.getBalance().compareTo(amount) < 0) {
            throw new CurrencyConversionException("Not enough VDollars on balance", HttpStatus.BAD_REQUEST);
        }
    }

    private OutboxEvent createCurrencyReservationEvent(Long aggregateId, CurrencyReservationPayload payload, Applicant applicant) {
        return OutboxEvent.builder()
                .aggregateId(aggregateId)
                .type(EventType.RESERVATION)
                .payload(toJson(payload))
                .status(EventStatus.PENDING_CONFIRMATION)
                .applicant(applicant)
                .createdAt(Instant.now())
                .version(0)
                .build();
    }

    public Wallet completeCurrencyReservation(OutboxEvent event) {
        CurrencyReservationPayload payload = fromJson(event.getPayload(), CurrencyReservationPayload.class);
        Wallet wallet = walletService.getById(event.getAggregateId());
        NotificationEvent notification = null;

        if (payload.getType().equals(ReservationType.WITHDRAWAL)) {
            wallet.setReserved(wallet.getReserved().subtract(payload.getAmount()));
        } else {
            BigDecimal toAdd = payload.getAmount();
            wallet.setBalance(wallet.getBalance().add(toAdd));
            notification = new NotificationEvent(NotificationType.BALANCE,  toAdd + " added to your balance", wallet.getBalance(), wallet.getId());
        }

        if (notification != null) {
            NotificationEvent finalNotification = notification;
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    redisTemplate.convertAndSend(Channel.NOTIFICATIONS.getName(), toJson(finalNotification));
                }
            });
        }

        return wallet;
    }

    public Wallet cancelCurrencyReservation(OutboxEvent event) {
        CurrencyReservationPayload payload = fromJson(event.getPayload(), CurrencyReservationPayload.class);
        Wallet wallet = null;

        if (payload.getType().equals(ReservationType.WITHDRAWAL)) {
            wallet = walletService.getById(event.getAggregateId());
            wallet.setBalance(wallet.getBalance().add(payload.getAmount()));
            wallet.setReserved(wallet.getReserved().subtract(payload.getAmount()));
        }

        return wallet;
    }

    private String toJson(Object o) {
        try {
            return objectMapper.writeValueAsString(o);
        } catch (JsonProcessingException e) {
            log.error("Error converting object to string {}", o, e);
            throw new AppException("Error serializing the object", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private <T> T fromJson(String data, Class<T> clazz) {
        try {
            return objectMapper.readValue(data, clazz);
        } catch (JsonProcessingException e) {
            log.error("Failed to deserialize object", e);
            throw new RuntimeException(e);
        }
    }
}
