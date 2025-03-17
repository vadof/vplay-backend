package com.vcasino.clicker.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vcasino.clicker.config.constants.CurrencyConstants;
import com.vcasino.clicker.dto.CurrencyConversionRequest;
import com.vcasino.clicker.entity.Account;
import com.vcasino.clicker.entity.OutboxEvent;
import com.vcasino.clicker.entity.enums.EventStatus;
import com.vcasino.clicker.entity.enums.EventType;
import com.vcasino.clicker.exception.AppException;
import com.vcasino.clicker.mock.AccountMocks;
import com.vcasino.clicker.repository.OutboxEventRepository;
import com.vcasino.common.enums.Currency;
import com.vcasino.common.kafka.Topic;
import com.vcasino.common.kafka.event.CurrencyConversionEvent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * UNIT tests for {@link CurrencyService}
 */
@ExtendWith(MockitoExtension.class)
public class CurrencyServiceTest {

    @Mock
    AccountService accountService;
    @Mock
    OutboxEventRepository outboxEventRepository;
    @Mock
    ObjectMapper objectMapper;
    @Mock
    KafkaTemplate<String, Object> kafkaTemplate;

    @InjectMocks
    CurrencyService currencyService;

    @Test
    @DisplayName("Convert currency VCoinsToVDollars")
    void convertCurrencyVCoinsToVDollars() {
        Account account = mockAccount(CurrencyConstants.MINIMUM_VCOINS_AMOUNT_TO_CONVERT.intValue());
        mockEvent(account.getId());

        try (MockedStatic<TransactionSynchronizationManager> mockedStatic = Mockito.mockStatic(TransactionSynchronizationManager.class)) {
            mockedStatic.when(() -> TransactionSynchronizationManager.registerSynchronization(any(TransactionSynchronization.class)))
                    .thenAnswer(invocation -> {
                        TransactionSynchronization synchronization = invocation.getArgument(0);
                        synchronization.afterCommit();
                        return null;
                    });

            CurrencyConversionRequest request = mockRequest(account.getBalanceCoins());
            currencyService.convertCurrency(request, account.getId());

            verify(outboxEventRepository, times(1)).save(any(OutboxEvent.class));
            verify(kafkaTemplate, times(1))
                    .send(eq(Topic.CURRENCY_CONVERSION.getName()), any(CurrencyConversionEvent.class));

            assertEquals(BigDecimal.ZERO, account.getBalanceCoins());
        }
    }

    @Test
    @DisplayName("Convert currency VCoinsToVDollars rounds to thousands")
    void convertCurrencyVCoinsToVDollarsRoundsToThousands() {
        Account account = mockAccount(134300);
        mockEvent(account.getId());

        try (MockedStatic<TransactionSynchronizationManager> mockedStatic = Mockito.mockStatic(TransactionSynchronizationManager.class)) {
            mockedStatic.when(() -> TransactionSynchronizationManager.registerSynchronization(any(TransactionSynchronization.class)))
                    .thenAnswer(invocation -> {
                        TransactionSynchronization synchronization = invocation.getArgument(0);
                        synchronization.afterCommit();
                        return null;
                    });

            CurrencyConversionRequest request = mockRequest(account.getBalanceCoins());
            currencyService.convertCurrency(request, account.getId());

            verify(outboxEventRepository, times(1)).save(any(OutboxEvent.class));
            verify(kafkaTemplate, times(1))
                    .send(eq(Topic.CURRENCY_CONVERSION.getName()), any(CurrencyConversionEvent.class));

            assertEquals(new BigDecimal(300), account.getBalanceCoins());
        }
    }

    @Test
    @DisplayName("Convert currency VCoinsToVDollars not enough balance")
    void convertCurrencyVCoinsToVDollarsNotEnoughBalance() {
        Account account = mockAccount(CurrencyConstants.MINIMUM_VCOINS_AMOUNT_TO_CONVERT.intValue() - 1);

        CurrencyConversionRequest request = mockRequest(account.getBalanceCoins());
        assertThrows(AppException.class, () -> currencyService.convertCurrency(request, account.getId()));
    }

    @Test
    @DisplayName("Convert currency VCoinsToVDollars incorrect conversion currencies")
    void convertCurrencyVCoinsToVDollarsIncorrectConversionCurrencies() {
        Account account = mockAccount(CurrencyConstants.MINIMUM_VCOINS_AMOUNT_TO_CONVERT.intValue());

        CurrencyConversionRequest request = mockRequest(account.getBalanceCoins());
        request.setConvertFrom(Currency.VCOIN);
        request.setConvertTo(Currency.VCOIN);

        assertThrows(AppException.class, () -> currencyService.convertCurrency(request, account.getId()));

        request.setConvertFrom(Currency.VDOLLAR);
        request.setConvertTo(Currency.VDOLLAR);

        assertThrows(AppException.class, () -> currencyService.convertCurrency(request, account.getId()));
    }

    private CurrencyConversionRequest mockRequest(BigDecimal amount) {
        return new CurrencyConversionRequest(Currency.VCOIN, Currency.VDOLLAR, amount);
    }

    private Account mockAccount(Integer balance) {
        Account account = AccountMocks.getAccountMock(1L);
        if (balance == null) {
            account.setBalanceCoins(BigDecimal.ZERO);
        } else {
            account.setBalanceCoins(new BigDecimal(balance));
        }
        when(accountService.getById(account.getId())).thenReturn(account);
        return account;
    }

    private OutboxEvent mockEvent(Long accountId) {
        OutboxEvent event = OutboxEvent.builder()
                .id(UUID.randomUUID())
                .aggregateId(accountId)
                .eventType(EventType.CURRENCY_CONVERSION)
                .status(EventStatus.IN_PROGRESS)
                .build();

        when(outboxEventRepository.save(any(OutboxEvent.class))).thenReturn(event);

        return event;
    }
}
