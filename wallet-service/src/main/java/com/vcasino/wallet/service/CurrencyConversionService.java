package com.vcasino.wallet.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vcasino.common.enums.Currency;
import com.vcasino.wallet.client.EventCreatedResponse;
import com.vcasino.wallet.config.ConversionConstants;
import com.vcasino.wallet.entity.CurrencyConversionPayload;
import com.vcasino.wallet.entity.OutboxEvent;
import com.vcasino.wallet.entity.Wallet;
import com.vcasino.wallet.entity.enums.Applicant;
import com.vcasino.wallet.entity.enums.EventStatus;
import com.vcasino.wallet.entity.enums.EventType;
import com.vcasino.wallet.exception.CurrencyConversionException;
import com.vcasino.wallet.repository.OutboxEventRepository;
import com.vcasino.wallet.client.InternalCurrencyConversionRequest;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;

@Service
@AllArgsConstructor
@Slf4j
public class CurrencyConversionService {

    private final OutboxEventRepository outboxEventRepository;
    private final WalletService walletService;
    private final ObjectMapper objectMapper;

    @Transactional
    public EventCreatedResponse convertCurrency(InternalCurrencyConversionRequest request) {
        if (request.getFrom().equals(Currency.VCOIN) && request.getTo().equals(Currency.VDOLLAR)) {
            return convertVCoinsToVDollars(request);
        } else if (request.getFrom().equals(Currency.VDOLLAR) && request.getTo().equals(Currency.VCOIN)) {
            return convertVDollarsToVCoins(request);
        } else {
            throw new CurrencyConversionException("Invalid conversion currencies", HttpStatus.BAD_REQUEST);
        }
    }

    @Transactional
    public EventCreatedResponse convertVCoinsToVDollars(InternalCurrencyConversionRequest request) {
        Wallet wallet = walletService.getById(request.getAccountId());

        CurrencyConversionPayload payload =
                new CurrencyConversionPayload(Currency.VCOIN, Currency.VDOLLAR, request.getAmount());

        OutboxEvent outboxEvent = createCurrencyConversionEvent(request.getAccountId(), payload);

        outboxEvent = outboxEventRepository.save(outboxEvent);

        return new EventCreatedResponse(outboxEvent.getId(), wallet.getBalance());
    }

    @Transactional
    public EventCreatedResponse convertVDollarsToVCoins(InternalCurrencyConversionRequest request) {
        Wallet wallet = walletService.getById(request.getAccountId());
        BigDecimal amount = request.getAmount();

        validateBalance(wallet, amount);

        wallet.setBalance(wallet.getBalance().subtract(amount));
        wallet.setReserved(wallet.getReserved().add(amount));

        CurrencyConversionPayload payload =
                new CurrencyConversionPayload(Currency.VDOLLAR, Currency.VCOIN, amount);

        OutboxEvent outboxEvent = createCurrencyConversionEvent(wallet.getId(), payload);

        wallet = walletService.save(wallet);
        outboxEvent = outboxEventRepository.save(outboxEvent);

        return new EventCreatedResponse(outboxEvent.getId(), wallet.getBalance());
    }

    public Wallet completeCurrencyConversion(OutboxEvent event) {
        CurrencyConversionPayload payload = fromJson(event.getPayload(), CurrencyConversionPayload.class);
        Wallet wallet = null;

        if (payload.getFrom().equals(Currency.VCOIN) && payload.getTo().equals(Currency.VDOLLAR))
        {
            wallet = walletService.getById(event.getAggregateId());
            BigDecimal toAdd = payload.getAmount().divide(ConversionConstants.VCOINS_TO_VDOLLARS_DIVIDER, 2, RoundingMode.DOWN);
            wallet.setBalance(wallet.getBalance().add(toAdd));
        }
        else if (payload.getFrom().equals(Currency.VDOLLAR) && payload.getTo().equals(Currency.VCOIN))
        {
            wallet = walletService.getById(event.getAggregateId());
            wallet.setReserved(wallet.getReserved().subtract(payload.getAmount()));
        }
        else {
            log.error("Cannot finish currency conversion. Invalid currencies {} -> {}",
                    payload.getFrom(), payload.getTo());
        }

        return wallet;
    }

    public Wallet cancelCurrencyConversion(OutboxEvent event) {
        CurrencyConversionPayload payload = fromJson(event.getPayload(), CurrencyConversionPayload.class);

        Wallet wallet = null;

        if (payload.getFrom().equals(Currency.VDOLLAR) && payload.getTo().equals(Currency.VCOIN))
        {
            wallet = walletService.getById(event.getAggregateId());
            wallet.setBalance(wallet.getBalance().add(payload.getAmount()));
            wallet.setReserved(wallet.getReserved().subtract(payload.getAmount()));
        }
        else if (!(payload.getFrom().equals(Currency.VCOIN) && payload.getTo().equals(Currency.VDOLLAR))) {
            log.error("Cannot finish currency conversion. Invalid currencies {} -> {}",
                    payload.getFrom(), payload.getTo());
        }

        return wallet;
    }

    private OutboxEvent createCurrencyConversionEvent(Long aggregateId, CurrencyConversionPayload payload) {
        return OutboxEvent.builder()
                .aggregateId(aggregateId)
                .type(EventType.CURRENCY_CONVERSION)
                .payload(toJson(payload))
                .status(EventStatus.PENDING_CONFIRMATION)
                .applicant(Applicant.CLICKER)
                .createdAt(Instant.now())
                .version(0)
                .build();
    }

    private void validateBalance(Wallet wallet, BigDecimal amount) {
        if (wallet.getBalance().compareTo(amount) < 0) {
            throw new CurrencyConversionException("Not enough VDollars on balance", HttpStatus.BAD_REQUEST);
        }
    }

    private String toJson(Object o) {
        try {
            return objectMapper.writeValueAsString(o);
        } catch (JsonProcessingException e) {
            log.error("Error converting object to string {}", o, e);
            throw new CurrencyConversionException("Error serializing the object", HttpStatus.INTERNAL_SERVER_ERROR);
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
