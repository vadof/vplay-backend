package com.vcasino.wallet.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vcasino.commonredis.enums.Channel;
import com.vcasino.commonredis.enums.NotificationType;
import com.vcasino.commonredis.event.NotificationEvent;
import com.vcasino.wallet.dto.BalanceDto;
import com.vcasino.wallet.dto.DepositRequestDto;
import com.vcasino.wallet.entity.DepositPayload;
import com.vcasino.wallet.entity.OutboxEvent;
import com.vcasino.wallet.entity.ReferralBonus;
import com.vcasino.wallet.entity.Wallet;
import com.vcasino.wallet.entity.enums.Applicant;
import com.vcasino.wallet.entity.enums.EventStatus;
import com.vcasino.wallet.entity.enums.EventType;
import com.vcasino.wallet.exception.AppException;
import com.vcasino.wallet.repository.OutboxEventRepository;
import com.vcasino.wallet.repository.WalletRepository;
import jakarta.annotation.Nullable;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;

@Service
@AllArgsConstructor
@Slf4j
public class WalletService {

    private final WalletRepository walletRepository;
    private final OutboxEventRepository outboxEventRepository;
    private final ObjectMapper objectMapper;
    private final StringRedisTemplate redisTemplate;

    @Transactional
    public void createWallet(Long id, @Nullable Long invitedBy) {
        ReferralBonus referralBonus = null;
        Wallet invitedByWallet = null;
        if (invitedBy != null) {
            invitedByWallet = getById(invitedBy);
            referralBonus = invitedByWallet.getReferralBonus();
        }

        Wallet wallet = Wallet.builder()
                .id(id)
                .balance(BigDecimal.ZERO)
                .reserved(BigDecimal.ZERO)
                .invitedBy(invitedByWallet)
                .updatedAt(Instant.now())
                .frozen(false)
                .version(0)
                .build();

        wallet = save(wallet);

        if (referralBonus != null) {
            depositToWallet(wallet.getId(), referralBonus.getAmount(), invitedBy);
        }

        log.info("Wallet#{} saved to database", id);
    }

    public BalanceDto getBalance(Long walletId) {
        return new BalanceDto(getById(walletId).getBalance());
    }

    public Wallet getById(Long id) {
        return walletRepository.findById(id).orElseThrow(
                () -> {
                    String message = "Wallet#" + id + " not found";
                    log.error(message);
                    return new AppException(message, HttpStatus.NOT_FOUND);
                });
    }

    public Wallet save(Wallet wallet) {
        return walletRepository.save(wallet);
    }

    @Transactional
    public BalanceDto depositToWallet(DepositRequestDto request, Long loggedInUserId) {
        return depositToWallet(request.getWalletId(), request.getAmount(), loggedInUserId);
    }

    private BalanceDto depositToWallet(Long walletId, BigDecimal amount, Long depositBy) {
        Wallet wallet = getById(walletId);

        BigDecimal toAdd = amount.setScale(2, RoundingMode.DOWN);
        wallet.setBalance(wallet.getBalance().add(toAdd));
        save(wallet);

        createDepositEvent(wallet.getId(), toAdd, depositBy);

        NotificationEvent notification = new NotificationEvent(NotificationType.BALANCE,  toAdd + " added to your balance", wallet.getBalance(), wallet.getId());
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                redisTemplate.convertAndSend(Channel.NOTIFICATIONS.getName(), toJson(notification));
            }
        });

        return new BalanceDto(wallet.getBalance());
    }

    private void createDepositEvent(Long walletId, BigDecimal amount, Long senderId) {
        OutboxEvent event = OutboxEvent.builder()
                .aggregateId(walletId)
                .type(EventType.DEPOSIT)
                .payload(toJson(new DepositPayload(amount, senderId)))
                .status(EventStatus.COMPLETED)
                .applicant(Applicant.WALLET)
                .createdAt(Instant.now())
                .version(0)
                .build();

        outboxEventRepository.save(event);
    }

    private String toJson(Object o) {
        try {
            return objectMapper.writeValueAsString(o);
        } catch (JsonProcessingException e) {
            log.error("Error converting object to string {}", o, e);
            throw new AppException("Error serializing the object", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
