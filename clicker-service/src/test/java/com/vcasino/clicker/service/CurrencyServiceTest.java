package com.vcasino.clicker.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vcasino.clicker.client.EventCreatedResponse;
import com.vcasino.clicker.client.InternalCurrencyConversionRequest;
import com.vcasino.clicker.client.WalletClient;
import com.vcasino.clicker.config.constants.CurrencyConstants;
import com.vcasino.clicker.dto.AccountWalletResponse;
import com.vcasino.clicker.dto.CurrencyConversionRequest;
import com.vcasino.clicker.entity.Account;
import com.vcasino.clicker.entity.Transaction;
import com.vcasino.clicker.exception.AppException;
import com.vcasino.clicker.mock.AccountMocks;
import com.vcasino.clicker.repository.TransactionRepository;
import com.vcasino.commonkafka.enums.Currency;
import com.vcasino.commonkafka.enums.Topic;
import com.vcasino.commonkafka.event.CompletedEvent;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
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
    WalletClient walletClient;
    @Mock
    TransactionRepository transactionRepository;
    @Mock
    ObjectMapper objectMapper;
    @Mock
    AsyncKafkaPublisher asyncKafkaPublisher;
    @Captor
    private ArgumentCaptor<Transaction> transactionArgumentCaptor;

    @InjectMocks
    CurrencyService currencyService;

    @Test
    @DisplayName("Convert currency VCoinsToVDollars")
    void convertCurrencyVCoinsToVDollars() {
        Account account = mockAccount(CurrencyConstants.MINIMUM_VCOINS_TO_CONVERT.intValue());

        try (MockedStatic<TransactionSynchronizationManager> mockedStatic = Mockito.mockStatic(TransactionSynchronizationManager.class)) {
            mockedStatic.when(() -> TransactionSynchronizationManager.registerSynchronization(any(TransactionSynchronization.class)))
                    .thenAnswer(invocation -> {
                        TransactionSynchronization synchronization = invocation.getArgument(0);
                        synchronization.afterCommit();
                        return null;
                    });

            BigDecimal amount = CurrencyConstants.MINIMUM_VCOINS_TO_CONVERT;

            InternalCurrencyConversionRequest request =
                    new InternalCurrencyConversionRequest(Currency.VCOIN, Currency.VDOLLAR, amount, account.getId());
            EventCreatedResponse feignResponse = mockFeignResponse(request);

            currencyService.convertToVDollars(new CurrencyConversionRequest(amount), account.getId());

            verify(transactionRepository, times(1)).save(transactionArgumentCaptor.capture());
            Transaction createdTransaction = transactionArgumentCaptor.getValue();
            assertEquals(feignResponse.getEventId(), createdTransaction.getEventId());
            assertEquals(account, createdTransaction.getAccount());
            assertEquals(amount, createdTransaction.getAmount());

            verify(accountService, times(1)).save(account);
            verify(asyncKafkaPublisher, times(1))
                    .send(Topic.COMPLETED_EVENTS, new CompletedEvent(feignResponse.getEventId()), true);

            assertEquals(BigDecimal.ZERO, account.getBalanceCoins());
        }
    }

    @Test
    @DisplayName("Convert currency VCoinsToVDollars rounds to thousands")
    void convertCurrencyVCoinsToVDollarsRoundsToThousands() {
        BigDecimal amount = new BigDecimal("134300.479823");

        Account account = mockAccount(amount.intValue());

        try (MockedStatic<TransactionSynchronizationManager> mockedStatic = Mockito.mockStatic(TransactionSynchronizationManager.class)) {
            mockedStatic.when(() -> TransactionSynchronizationManager.registerSynchronization(any(TransactionSynchronization.class)))
                    .thenAnswer(invocation -> {
                        TransactionSynchronization synchronization = invocation.getArgument(0);
                        synchronization.afterCommit();
                        return null;
                    });

            InternalCurrencyConversionRequest request =
                    new InternalCurrencyConversionRequest(Currency.VCOIN, Currency.VDOLLAR, new BigDecimal(134000), account.getId());
            EventCreatedResponse feignResponse = mockFeignResponse(request);

            currencyService.convertToVDollars(new CurrencyConversionRequest(amount), account.getId());

            assertEquals(new BigDecimal(300), account.getBalanceCoins());
            verify(accountService, times(1)).save(account);
            verify(asyncKafkaPublisher, times(1))
                    .send(Topic.COMPLETED_EVENTS, new CompletedEvent(feignResponse.getEventId()), true);
        }
    }

    @Test
    @DisplayName("Convert currency VCoinsToVDollars not enough balance")
    void convertCurrencyVCoinsToVDollarsNotEnoughBalance() {
        BigDecimal amount = CurrencyConstants.MINIMUM_VCOINS_TO_CONVERT;
        Account account = mockAccount(amount.intValue() - 1);

        AppException exception = assertThrows(AppException.class,
                () -> currencyService.convertToVDollars(new CurrencyConversionRequest(amount), account.getId()));

        assertTrue(exception.getMessage().contains("Not enough"));
        assertEquals(HttpStatus.BAD_REQUEST, exception.getHttpStatus());
        verify(asyncKafkaPublisher, times(0)).send(any(), any(), eq(true));
    }

    @Test
    @DisplayName("Convert currency VCoinsToVDollars minimum amount to convert")
    void convertCurrencyVCoinsToVDollarsMinimumAmountToConvert() {
        BigDecimal amount = CurrencyConstants.MINIMUM_VCOINS_TO_CONVERT.subtract(BigDecimal.ONE);
        Account account = mockAccount(amount.intValue() + 1);

        AppException exception = assertThrows(AppException.class,
                () -> currencyService.convertToVDollars(new CurrencyConversionRequest(amount), account.getId()));

        assertTrue(exception.getMessage().contains("minimum"));
        assertEquals(HttpStatus.BAD_REQUEST, exception.getHttpStatus());
        verify(asyncKafkaPublisher, times(0)).send(any(), any(), eq(true));
    }

    @Test
    @DisplayName("Convert currency VCoinsToVDollars feign exception")
    void convertCurrencyVCoinsToVDollarsFeignException() {
        BigDecimal amount = CurrencyConstants.MINIMUM_VCOINS_TO_CONVERT;
        Account account = mockAccount(amount.intValue());

        InternalCurrencyConversionRequest request =
                new InternalCurrencyConversionRequest(Currency.VCOIN, Currency.VDOLLAR, amount, account.getId());

        when(walletClient.convertCurrency(request)).thenThrow(new RuntimeException(""));

        AppException exception = assertThrows(AppException.class,
                () -> currencyService.convertToVDollars(new CurrencyConversionRequest(amount), account.getId()));

        assertTrue(exception.getMessage().contains("failed"));
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, exception.getHttpStatus());
        verify(asyncKafkaPublisher, times(0)).send(any(), any(), eq(true));
    }

    @Test
    @DisplayName("Convert currency VDollarsToVCoins")
    void convertCurrencyVDollarsToVCoins() {
        Account account = mockAccount(0);
        BigDecimal amount = CurrencyConstants.MINIMUM_VDOLLARS_TO_CONVERT;

        try (MockedStatic<TransactionSynchronizationManager> mockedStatic = Mockito.mockStatic(TransactionSynchronizationManager.class)) {
            mockedStatic.when(() -> TransactionSynchronizationManager.registerSynchronization(any(TransactionSynchronization.class)))
                    .thenAnswer(invocation -> {
                        TransactionSynchronization synchronization = invocation.getArgument(0);
                        synchronization.afterCommit();
                        return null;
                    });

            InternalCurrencyConversionRequest request =
                    new InternalCurrencyConversionRequest(Currency.VDOLLAR, Currency.VCOIN, amount, account.getId());
            EventCreatedResponse feignResponse = mockFeignResponse(request);

            AccountWalletResponse walletResponse = currencyService.convertToVCoins(new CurrencyConversionRequest(amount), account.getId());

            verify(transactionRepository, times(1)).save(transactionArgumentCaptor.capture());
            Transaction createdTransaction = transactionArgumentCaptor.getValue();
            assertEquals(feignResponse.getEventId(), createdTransaction.getEventId());
            assertEquals(account, createdTransaction.getAccount());
            assertEquals(amount, createdTransaction.getAmount());

            verify(accountService, times(1)).save(account);
            verify(asyncKafkaPublisher, times(1))
                    .send(Topic.COMPLETED_EVENTS, new CompletedEvent(feignResponse.getEventId()), true);

            assertEquals(CurrencyConstants.VDOLLARS_TO_VCOINS_MULTIPLIER.multiply(amount), account.getBalanceCoins());
            assertEquals(walletResponse.getUpdatedWalletBalance(), feignResponse.getUpdatedBalance());
        }
    }

    @Test
    @DisplayName("Convert currency VDollarsToVCoins scales to precision 2")
    void convertCurrencyVDollarsToVCoinsScalesCorrectly() {
        Account account = mockAccount(0);

        try (MockedStatic<TransactionSynchronizationManager> mockedStatic = Mockito.mockStatic(TransactionSynchronizationManager.class)) {
            mockedStatic.when(() -> TransactionSynchronizationManager.registerSynchronization(any(TransactionSynchronization.class)))
                    .thenAnswer(invocation -> {
                        TransactionSynchronization synchronization = invocation.getArgument(0);
                        synchronization.afterCommit();
                        return null;
                    });

            InternalCurrencyConversionRequest request =
                    new InternalCurrencyConversionRequest(Currency.VDOLLAR, Currency.VCOIN, new BigDecimal("1.10"), account.getId());
            EventCreatedResponse feignResponse = mockFeignResponse(request);

            AccountWalletResponse walletResponse = currencyService.convertToVCoins(
                    new CurrencyConversionRequest(new BigDecimal("1.10437534")), account.getId());

            verify(accountService, times(1)).save(account);
            verify(asyncKafkaPublisher, times(1))
                    .send(Topic.COMPLETED_EVENTS, new CompletedEvent(feignResponse.getEventId()), true);

            assertEquals(CurrencyConstants.VDOLLARS_TO_VCOINS_MULTIPLIER.multiply(new BigDecimal("1.10")), account.getBalanceCoins());
            assertEquals(walletResponse.getUpdatedWalletBalance(), feignResponse.getUpdatedBalance());
        }
    }

    @Test
    @DisplayName("Convert currency VDollarsToVCoins minimum amount to convert")
    void convertCurrencyVDollarsToVCoinsMinimumAmountToConvert() {
        BigDecimal amount = CurrencyConstants.MINIMUM_VDOLLARS_TO_CONVERT.subtract(new BigDecimal("0.01"));
        Account account = mockAccount(0);

        AppException exception = assertThrows(AppException.class,
                () -> currencyService.convertToVCoins(new CurrencyConversionRequest(amount), account.getId()));

        assertTrue(exception.getMessage().contains("minimum"));
        assertEquals(HttpStatus.BAD_REQUEST, exception.getHttpStatus());
        assertEquals(account.getBalanceCoins(), BigDecimal.ZERO);
        verify(asyncKafkaPublisher, times(0)).send(any(), any(), eq(true));
    }

    @Test
    @DisplayName("Convert currency VDollarsToVCoins feign exception")
    void convertCurrencyVDollarsToVCoinsFeignException() {
        BigDecimal amount = CurrencyConstants.MINIMUM_VDOLLARS_TO_CONVERT;
        Account account = mockAccount(0);

        InternalCurrencyConversionRequest request =
                new InternalCurrencyConversionRequest(Currency.VDOLLAR, Currency.VCOIN, amount, account.getId());

        when(walletClient.convertCurrency(request)).thenThrow(new RuntimeException(""));

        AppException exception = assertThrows(AppException.class,
                () -> currencyService.convertToVCoins(new CurrencyConversionRequest(amount), account.getId()));

        assertTrue(exception.getMessage().contains("failed"));
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, exception.getHttpStatus());
        assertEquals(account.getBalanceCoins(), BigDecimal.ZERO);
        verify(asyncKafkaPublisher, times(0)).send(any(), any(), eq(true));
    }


    private EventCreatedResponse mockFeignResponse(InternalCurrencyConversionRequest request) {
        EventCreatedResponse eventCreatedResponse = new EventCreatedResponse(UUID.randomUUID(), BigDecimal.ZERO);
        when(walletClient.convertCurrency(request)).thenReturn(ResponseEntity.of(Optional.of(eventCreatedResponse)));
        return eventCreatedResponse;
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

}
