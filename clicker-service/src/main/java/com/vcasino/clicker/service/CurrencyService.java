package com.vcasino.clicker.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vcasino.clicker.config.constants.CurrencyConstants;
import com.vcasino.clicker.dto.AccountDto;
import com.vcasino.clicker.dto.CurrencyConversionRequest;
import com.vcasino.clicker.entity.Account;
import com.vcasino.clicker.entity.OutboxEvent;
import com.vcasino.clicker.entity.enums.EventStatus;
import com.vcasino.clicker.entity.enums.EventType;
import com.vcasino.clicker.exception.AppException;
import com.vcasino.clicker.repository.OutboxEventRepository;
import com.vcasino.common.enums.Currency;
import com.vcasino.common.kafka.Topic;
import com.vcasino.common.kafka.event.CurrencyConversionEvent;
import com.vcasino.common.kafka.event.CurrencyConversionPayload;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
@Slf4j
public class CurrencyService {

    private final AccountService accountService;
    private final OutboxEventRepository outboxEventRepository;
    private final ObjectMapper objectMapper;

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Autowired
    public CurrencyService(AccountService accountService, OutboxEventRepository outboxEventRepository,
                           ObjectMapper objectMapper,
                           @Qualifier("defaultRetryTopicKafkaTemplate") KafkaTemplate<String, Object> kafkaTemplate) {
        this.accountService = accountService;
        this.outboxEventRepository = outboxEventRepository;
        this.objectMapper = objectMapper;
        this.kafkaTemplate = kafkaTemplate;
    }

    @Transactional
    public AccountDto convertCurrency(CurrencyConversionRequest conversionRequest, Long accountId) {
        Account account = accountService.getById(accountId);
        accountService.updateAccount(account);

        CurrencyConversionEvent currencyConversionEvent;
        if (conversionRequest.getConvertFrom().equals(Currency.VCOIN)
                && conversionRequest.getConvertTo().equals(Currency.VDOLLAR)) {
            currencyConversionEvent = convertCoinsToDollars(account, conversionRequest.getAmount());
        } else {
            throw new AppException("Incorrect conversion currencies", HttpStatus.BAD_REQUEST);
        }

        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                kafkaTemplate.send(Topic.CURRENCY_CONVERSION.getName(), currencyConversionEvent);
            }
        });

        return accountService.toDto(accountService.save(account));
    }

    private CurrencyConversionEvent convertCoinsToDollars(Account account, BigDecimal amount) {
        amount = roundToNearestThousand(amount);

        validateBalance(account, amount);
        validateMinimumAmount(amount, CurrencyConstants.MINIMUM_VCOINS_AMOUNT_TO_CONVERT, Currency.VCOIN);

        account.setBalanceCoins(account.getBalanceCoins().subtract(amount));

        CurrencyConversionPayload payload = new CurrencyConversionPayload(Currency.VCOIN, Currency.VDOLLAR, amount);

        OutboxEvent event = OutboxEvent.builder()
                .aggregateId(account.getId())
                .eventType(EventType.CURRENCY_CONVERSION)
                .payload(writeValueAsString(payload))
                .status(EventStatus.IN_PROGRESS)
                .build();

        event = outboxEventRepository.save(event);

        log.info("Currency Conversion Event - '{}' for Account#{} created", event.getId(), event.getAggregateId());

        return new CurrencyConversionEvent(event.getId(), event.getAggregateId(), payload);
    }

    private CurrencyConversionEvent convertDollarsToCoins(Account account, BigDecimal amount) {
        return null;
    }

    private void validateMinimumAmount(BigDecimal amount, BigDecimal minAmount, Currency currency) {
        if (amount.compareTo(minAmount) < 0) {
            throw new AppException("The minimum amount for conversion is %s %s"
                    .formatted(minAmount, currency.getName()), HttpStatus.BAD_REQUEST);
        }
    }

    private void validateBalance(Account account, BigDecimal amount) {
        if (account.getBalanceCoins().compareTo(amount) < 0) {
            throw new AppException("Not enough VCoins on balance", HttpStatus.BAD_REQUEST);
        }
    }

    public BigDecimal roundToNearestThousand(BigDecimal amount) {
        return amount.divide(new BigDecimal("1000"), 0, RoundingMode.DOWN)
                .multiply(new BigDecimal("1000"));
    }

    private String writeValueAsString(Object o) {
        try {
            return objectMapper.writeValueAsString(o);
        } catch (JsonProcessingException e) {
            log.error("Error converting object to string {}", o, e);
            throw new AppException(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}
