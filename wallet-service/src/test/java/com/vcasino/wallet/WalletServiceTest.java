package com.vcasino.wallet;

import com.vcasino.common.enums.Currency;
import com.vcasino.common.kafka.Topic;
import com.vcasino.common.kafka.event.CurrencyConversionEvent;
import com.vcasino.common.kafka.event.CurrencyConversionPayload;
import com.vcasino.wallet.entity.ProcessedEvent;
import com.vcasino.wallet.entity.Wallet;
import com.vcasino.wallet.repository.ProcessedEventRepository;
import com.vcasino.wallet.repository.WalletRepository;
import com.vcasino.wallet.service.WalletService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * UNIT tests for {@link com.vcasino.wallet.service.WalletService}
 */
@ExtendWith(MockitoExtension.class)
public class WalletServiceTest {

    @Mock
    WalletRepository walletRepository;
    @Mock
    ProcessedEventRepository processedEventRepository;
    @Mock
    KafkaTemplate<String, Object> kafkaTemplate;

    @Captor
    ArgumentCaptor<ProcessedEvent> processedEventArgumentCaptor;

    @InjectMocks
    WalletService walletService;

    @Test
    @DisplayName("Convert VCoins to VDollars")
    void convertCurrencyVCoinsToVDollars() {
        CurrencyConversionEvent event = mockEvent(125000);
        Wallet wallet = mockWallet();
        Instant lastUpdatedAt = wallet.getUpdatedAt();

        try (MockedStatic<TransactionSynchronizationManager> mockedStatic = Mockito.mockStatic(TransactionSynchronizationManager.class)) {
            mockedStatic.when(() -> TransactionSynchronizationManager.registerSynchronization(any(TransactionSynchronization.class)))
                    .thenAnswer(invocation -> {
                        TransactionSynchronization synchronization = invocation.getArgument(0);
                        synchronization.afterCommit();
                        return null;
                    });

            walletService.convertCurrency(event);

            verify(processedEventRepository, times(1)).save(processedEventArgumentCaptor.capture());
            ProcessedEvent processedEvent = processedEventArgumentCaptor.getValue();

            assertEquals(event.eventId(), processedEvent.getEventId());
            assertNotNull(processedEvent.getProcessedAt());

            assertTrue(wallet.getUpdatedAt().isAfter(lastUpdatedAt));
            assertEquals(new BigDecimal("1.25"), wallet.getBalance());

            verify(kafkaTemplate, times(1)).send(Topic.PROCESSED_EVENTS.getName(), new com.vcasino.common.kafka.event.ProcessedEvent(event.eventId()));
        }
    }

    @Test
    @DisplayName("Convert currency duplicated event")
    void convertCurrencyDuplicatedEvent() {
        CurrencyConversionEvent event = mockEvent(125000);

        when(processedEventRepository.existsById(event.eventId())).thenReturn(true);

        walletService.convertCurrency(event);

        verify(walletRepository, times(0)).findById(any(Long.class));
        verify(kafkaTemplate, times(1)).send(Topic.PROCESSED_EVENTS.getName(), new com.vcasino.common.kafka.event.ProcessedEvent(event.eventId()));
    }

    @Test
    @DisplayName("Convert currency incorrect currencies")
    void convertCurrencyIncorrectCurrencies() {
        CurrencyConversionEvent event = new CurrencyConversionEvent(UUID.randomUUID(), 1L,
                new CurrencyConversionPayload(Currency.VCOIN, Currency.VCOIN, new BigDecimal(100000)));
        Wallet wallet = mockWallet();

        walletService.convertCurrency(event);

        event = new CurrencyConversionEvent(UUID.randomUUID(), 1L,
                new CurrencyConversionPayload(Currency.VDOLLAR, Currency.VDOLLAR, new BigDecimal(100000)));

        walletService.convertCurrency(event);

        verify(walletRepository, times(0)).save(wallet);

    }

    private CurrencyConversionEvent mockEvent(int amount) {
        return new CurrencyConversionEvent(UUID.randomUUID(), 1L,
                new CurrencyConversionPayload(Currency.VCOIN, Currency.VDOLLAR, new BigDecimal(amount)));
    }

    private Wallet mockWallet() {
        Wallet wallet = Wallet.builder()
                .id(1L)
                .balance(BigDecimal.ZERO)
                .updatedAt(LocalDateTime.of(2020, 1, 1, 0, 0).toInstant(ZoneOffset.UTC))
                .frozen(false)
                .version(0)
                .build();
        when(walletRepository.findById(wallet.getId())).thenReturn(Optional.of(wallet));
        return wallet;
    }
}
