package com.vcasino.bet.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vcasino.bet.client.EventCreatedResponse;
import com.vcasino.bet.client.EventStatus;
import com.vcasino.bet.client.EventStatusRequest;
import com.vcasino.bet.client.EventStatusResponse;
import com.vcasino.bet.client.ReservationRequest;
import com.vcasino.bet.client.ReservationType;
import com.vcasino.bet.client.WalletClient;
import com.vcasino.bet.dto.request.BetRequest;
import com.vcasino.bet.entity.Transaction;
import com.vcasino.bet.entity.User;
import com.vcasino.bet.exception.AppException;
import com.vcasino.bet.repository.TransactionRepository;
import com.vcasino.commonkafka.enums.Topic;
import com.vcasino.commonkafka.event.CompletedEvent;
import feign.FeignException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
@Slf4j
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final WalletClient walletClient;
    private final ObjectMapper objectMapper;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public EventCreatedResponse createWithdrawalEvent(BetRequest request, User user) {
        try {
            ReservationRequest withdrawalRequest = new ReservationRequest(request.getAmount(), "BET",
                    user.getId(), ReservationType.WITHDRAWAL);

            EventCreatedResponse response = walletClient.reserveCurrency(withdrawalRequest).getBody();

            Transaction transaction = new Transaction(response.getEventId(), request.getAmount(), user);
            transactionRepository.save(transaction);

            return response;
        } catch (FeignException e) {
            handleFeignError(e);
            throw betException(e);
        } catch (Exception e) {
            throw betException(e);
        }
    }

    public Optional<EventCreatedResponse> createDepositEvent(BigDecimal amount, User user) {
        try {
            ReservationRequest depositRequest = new ReservationRequest(amount, "BET",
                    user.getId(), ReservationType.DEPOSIT);

            EventCreatedResponse response = walletClient.reserveCurrency(depositRequest).getBody();

            Transaction transaction = new Transaction(response.getEventId(), amount, user);
            transactionRepository.save(transaction);

            return Optional.of(response);
        } catch (Exception e) {
            log.error("Error creating deposit event", e);
            return Optional.empty();
        }
    }

    @Transactional(readOnly = true)
    public EventStatusResponse getEventStatuses(EventStatusRequest request) {
        List<UUID> eventIds = request.getEventIds();
        if (eventIds.isEmpty()) {
            return new EventStatusResponse(Collections.emptyMap());
        }

        Set<UUID> existingEventIds = new HashSet<>(transactionRepository.findExistingEventIds(eventIds));

        Map<UUID, EventStatus> eventStatuses = eventIds.stream()
                .collect(Collectors.toMap(
                        id -> id,
                        id -> existingEventIds.contains(id) ? EventStatus.COMPLETED : EventStatus.CANCELLED
                ));

        return new EventStatusResponse(eventStatuses);
    }

    @Async
    public void sendCompletedEvent(UUID eventId) {
        kafkaTemplate.send(Topic.COMPLETED_EVENTS.getName(), new CompletedEvent(eventId));
    }

    @Async
    public void sendCompletedEvents(List<UUID> eventIds) {
        if (!eventIds.isEmpty()) {
            for (UUID eventId : eventIds) {
                kafkaTemplate.send(Topic.COMPLETED_EVENTS.getName(), new CompletedEvent(eventId));
            }
        }
    }

    private void handleFeignError(FeignException e) {
        if (e.status() == 400) {
            try {
                String message = objectMapper.readTree(e.contentUTF8()).get("message").asText();
                throw new AppException(message, HttpStatus.BAD_REQUEST);
            } catch (JsonProcessingException ex) {
                throw betException(e);
            }
        }
        throw betException(e);
    }

    private AppException betException(Exception e) {
        log.error("Bet processing failed", e);
        return new AppException("Bet processing failed", HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
