package com.vcasino.clicker.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vcasino.clicker.client.EventCreatedResponse;
import com.vcasino.clicker.client.InternalCurrencyConversionRequest;
import com.vcasino.clicker.client.WalletClient;
import com.vcasino.clicker.config.constants.CurrencyConstants;
import com.vcasino.clicker.dto.AccountDto;
import com.vcasino.clicker.dto.AccountWalletResponse;
import com.vcasino.clicker.dto.CurrencyConversionRequest;
import com.vcasino.clicker.entity.Account;
import com.vcasino.clicker.entity.Transaction;
import com.vcasino.clicker.exception.AppException;
import com.vcasino.clicker.repository.TransactionRepository;
import com.vcasino.common.enums.Currency;
import com.vcasino.common.kafka.Topic;
import com.vcasino.common.kafka.event.CompletedEvent;
import feign.FeignException;
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
    private final WalletClient walletClient;
    private final TransactionRepository transactionRepository;
    private final ObjectMapper objectMapper;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Autowired
    public CurrencyService(AccountService accountService, WalletClient walletClient,
                           TransactionRepository transactionRepository, ObjectMapper objectMapper,
                           @Qualifier("defaultRetryTopicKafkaTemplate") KafkaTemplate<String, Object> kafkaTemplate) {
        this.accountService = accountService;
        this.walletClient = walletClient;
        this.transactionRepository = transactionRepository;
        this.objectMapper = objectMapper;
        this.kafkaTemplate = kafkaTemplate;
    }

    @Transactional
    public AccountDto convertToVDollars(CurrencyConversionRequest conversionRequest, Long accountId) {
        Account account = accountService.getById(accountId);
        accountService.updateAccount(account);

        BigDecimal amount = roundToNearestThousand(conversionRequest.getAmount());

        validateBalance(account, amount);
        validateMinimumAmount(amount, CurrencyConstants.MINIMUM_VCOINS_TO_CONVERT);

        InternalCurrencyConversionRequest request =
                new InternalCurrencyConversionRequest(Currency.VCOIN, Currency.VDOLLAR, amount, account.getId());
        EventCreatedResponse response = createEvent(request, account);
        account.setBalanceCoins(account.getBalanceCoins().subtract(amount));

        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                kafkaTemplate.send(Topic.COMPLETED_EVENTS.getName(), new CompletedEvent(response.getEventId()));
            }
        });

        return accountService.toDto(accountService.save(account));
    }

    @Transactional
    public AccountWalletResponse convertToVCoins(CurrencyConversionRequest conversionRequest, Long accountId) {
        Account account = accountService.getById(accountId);
        BigDecimal amount = conversionRequest.getAmount().setScale(2, RoundingMode.DOWN);

        validateMinimumAmount(amount, CurrencyConstants.MINIMUM_VDOLLARS_TO_CONVERT);

        InternalCurrencyConversionRequest request =
                new InternalCurrencyConversionRequest(Currency.VDOLLAR, Currency.VCOIN, amount, account.getId());

        EventCreatedResponse response = createEvent(request, account);

        accountService.updateAccount(account);
        account.setBalanceCoins(account.getBalanceCoins()
                .add(amount.multiply(CurrencyConstants.VDOLLARS_TO_VCOINS_MULTIPLIER)));

        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                kafkaTemplate.send(Topic.COMPLETED_EVENTS.getName(), new CompletedEvent(response.getEventId()));
            }
        });

        return new AccountWalletResponse(accountService.toDto(accountService.save(account)), response.getUpdatedBalance());
    }

    private EventCreatedResponse createEvent(InternalCurrencyConversionRequest request, Account account) {
        try {
            EventCreatedResponse response = walletClient.convertCurrency(request).getBody();

            Transaction transaction = new Transaction(response.getEventId(), request.getAmount(), account);
            transactionRepository.save(transaction);

            return response;
        } catch (FeignException e) {
            handleFeignError(e);
            throw currencyConversionException(e);
        } catch (Exception e) {
            throw currencyConversionException(e);
        }
    }

    private void handleFeignError(FeignException e) {
        if (e.status() == 400) {
            try {
                String message = objectMapper.readTree(e.contentUTF8()).get("message").asText();
                throw new AppException(message, HttpStatus.BAD_REQUEST);
            } catch (JsonProcessingException ex) {
                throw currencyConversionException(e);
            }
        }
        throw currencyConversionException(e);
    }

    private AppException currencyConversionException(Exception e) {
        log.error("Currency conversion failed", e);
        return new AppException("Currency conversion failed, please try again later", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private void validateMinimumAmount(BigDecimal amount, BigDecimal minAmount) {
        if (amount.compareTo(minAmount) < 0) {
            throw new AppException("The minimum amount for conversion is " + minAmount, HttpStatus.BAD_REQUEST);
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
}
