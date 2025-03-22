package com.vcasino.wallet.service;

import com.vcasino.wallet.dto.BalanceDto;
import com.vcasino.wallet.entity.Wallet;
import com.vcasino.wallet.repository.WalletRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;

@Service
@AllArgsConstructor
@Slf4j
public class WalletService {

    private final WalletRepository walletRepository;

    @Transactional
    public void createWallet(Long id) {
        Wallet wallet = Wallet.builder()
                .id(id)
                .balance(BigDecimal.ZERO)
                .reserved(BigDecimal.ZERO)
                .updatedAt(Instant.now())
                .frozen(false)
                .version(0)
                .build();

        walletRepository.save(wallet);
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
                    return new RuntimeException(message);
                });
    }

    public Wallet save(Wallet wallet) {
        return walletRepository.save(wallet);
    }

}
