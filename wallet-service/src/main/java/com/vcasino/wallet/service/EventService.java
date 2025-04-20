package com.vcasino.wallet.service;

import com.vcasino.wallet.client.BetClient;
import com.vcasino.wallet.client.ClickerDataClient;
import com.vcasino.wallet.client.Client;
import com.vcasino.wallet.client.EventStatusRequest;
import com.vcasino.wallet.entity.OutboxEvent;
import com.vcasino.wallet.entity.enums.Applicant;
import com.vcasino.wallet.entity.enums.EventStatus;
import com.vcasino.wallet.repository.OutboxEventRepository;
import jakarta.annotation.PostConstruct;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.apache.commons.collections4.ListUtils.partition;

@Service
@AllArgsConstructor
@Slf4j
public class EventService {

    private final OutboxEventRepository outboxEventRepository;
    private final EventFinisherService eventFinisher;
    private final Map<Applicant, Client> clientMap = new HashMap<>();
    private final ClickerDataClient clickerDataClient;
    private final BetClient betClient;

    public void handlePendingConfirmationEvents() {
        int recordsPerApplicant = 50000;

        List<OutboxEvent> events = outboxEventRepository.findPerApplicantByStatusAndCreatedAtBefore(
                EventStatus.PENDING_CONFIRMATION.name(), Instant.now().minusSeconds(300), recordsPerApplicant);

        log.info("Found {} PENDING_CONFIRMATION events", events.size());
        if (events.isEmpty()) return;

        Map<Applicant, List<OutboxEvent>> eventsByApplicant = events.stream()
                .collect(Collectors.groupingBy(OutboxEvent::getApplicant, HashMap::new, Collectors.toList()));

        Map<UUID, OutboxEvent> eventMap = events.stream()
                .collect(Collectors.toMap(OutboxEvent::getId, Function.identity()));

        for (Map.Entry<Applicant, List<OutboxEvent>> entry : eventsByApplicant.entrySet()) {
            List<OutboxEvent> applicantEvents = entry.getValue();

            if (applicantEvents.isEmpty()) continue;

            int eventsPerThread = 10000;
            int maxThreadsPerApplicant = recordsPerApplicant / eventsPerThread;
            int threadCount = Math.min(
                    applicantEvents.size() / eventsPerThread + (applicantEvents.size() % eventsPerThread == 0 ? 0 : 1),
                    maxThreadsPerApplicant
            );

            handleInParallel(entry.getKey(), applicantEvents, eventMap, threadCount);
        }
    }

    private void handleInParallel(Applicant applicant, List<OutboxEvent> events, Map<UUID, OutboxEvent> eventMap, int threadCount) {
        Client client = clientMap.get(applicant);
        if (client == null) {
            log.error("Unknown applicant {}", applicant);
            return;
        }

        List<List<UUID>> partitions = partition(events.stream().map(OutboxEvent::getId).toList(), threadCount);

        try (ExecutorService executor = Executors.newFixedThreadPool(threadCount)) {
            CountDownLatch latch = new CountDownLatch(partitions.size());

            for (List<UUID> chunk : partitions) {
                executor.submit(() -> {
                    try {
                        handlePendingEventsByApplicant(client, chunk, eventMap);
                    } catch (Exception e) {
                        log.error("Error in applicant thread chunk", e);
                    } finally {
                        latch.countDown();
                    }
                });
            }

            latch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private void handlePendingEventsByApplicant(Client client, List<UUID> events, Map<UUID, OutboxEvent> eventMap) {
        if (events.isEmpty()) return;

        Map<UUID, EventStatus> eventStatuses;
        try {
            eventStatuses = client.getEventStatuses(new EventStatusRequest(events))
                    .getBody().getEventStatuses();

        } catch (Exception e) {
            log.error("Request to get event statuses failed, for {} events", client.getClass().getName(), e);
            throw new RuntimeException("Request to get response statuses failed");
        }

        if (eventStatuses == null || eventStatuses.isEmpty()) return;

        for (Map.Entry<UUID, EventStatus> eventEntry : eventStatuses.entrySet()) {
            OutboxEvent outboxEvent = eventMap.get(eventEntry.getKey());
            try {
                if (eventEntry.getValue().equals(EventStatus.COMPLETED)) {
                    eventFinisher.completeEvent(outboxEvent);
                } else {
                    eventFinisher.cancelEvent(outboxEvent);
                }
            } catch (Exception e) {
                log.error("Error completing/cancelling Event - {}", outboxEvent.getId(), e);
            }
        }
    }

    @PostConstruct
    private void initClientMap() {
        clientMap.put(Applicant.CLICKER, clickerDataClient);
        clientMap.put(Applicant.BET, betClient);
    }

}
