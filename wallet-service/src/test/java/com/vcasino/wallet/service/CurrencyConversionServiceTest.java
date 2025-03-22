package com.vcasino.wallet.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vcasino.common.enums.Currency;
import com.vcasino.wallet.entity.CurrencyConversionPayload;
import com.vcasino.wallet.entity.OutboxEvent;
import com.vcasino.wallet.entity.Wallet;
import com.vcasino.wallet.mock.OutboxEventMocks;
import com.vcasino.wallet.mock.WalletMocks;
import com.vcasino.wallet.repository.OutboxEventRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

/**
 * UNIT tests for {@link CurrencyConversionService}
 */
@ExtendWith(MockitoExtension.class)
public class CurrencyConversionServiceTest {

    @Mock
    OutboxEventRepository outboxEventRepository;
    @Mock
    WalletService walletService;

    ObjectMapper objectMapper = new ObjectMapper();

    CurrencyConversionService conversionService;

    @Captor
    ArgumentCaptor<Wallet> walletArgumentCaptor;

    @BeforeEach
    void setup() {
        conversionService = new CurrencyConversionService(outboxEventRepository, walletService, objectMapper);
    }

    @Test
    @DisplayName("Complete VCoins to VDollars currency conversion")
    void completeCurrencyConversionVCoinsToVDollars() throws Exception {
        CurrencyConversionPayload payload =
                new CurrencyConversionPayload(Currency.VCOIN, Currency.VDOLLAR, new BigDecimal("100000"));
        Wallet wallet = WalletMocks.getWalletMock();
        OutboxEvent event = OutboxEventMocks.getCurrencyConversionOutboxEventMock(wallet.getId(), payload);

        when(walletService.getById(wallet.getId())).thenReturn(wallet);

        wallet = conversionService.completeCurrencyConversion(event);

        assertEquals(wallet.getBalance(), new BigDecimal("1.00"));
    }

    @Test
    @DisplayName("Cancel VDollars to VCoins currency conversion")
    void cancelCurrencyConversionVDollarsToVCoins() throws Exception {
        Wallet wallet = WalletMocks.getWalletMock(new BigDecimal("0.00"));
        wallet.setReserved(new BigDecimal("1.00"));

        CurrencyConversionPayload payload =
                new CurrencyConversionPayload(Currency.VDOLLAR, Currency.VCOIN, new BigDecimal("1.00"));

        OutboxEvent event = OutboxEventMocks.getCurrencyConversionOutboxEventMock(wallet.getId(), payload);

        when(walletService.getById(wallet.getId())).thenReturn(wallet);

        wallet = conversionService.cancelCurrencyConversion(event);

        assertEquals(wallet.getReserved(), new BigDecimal("0.00"));
        assertEquals(wallet.getBalance(), new BigDecimal("1.00"));
    }

}
