package com.vcasino.wallet.mock;

import com.vcasino.wallet.entity.Wallet;

import java.math.BigDecimal;
import java.time.Instant;

public class WalletMocks {
    public static Wallet getWalletMock(BigDecimal balance) {
        return Wallet.builder()
                .id(1L)
                .balance(balance)
                .reserved(new BigDecimal("0.00"))
                .frozen(false)
                .version(0)
                .updatedAt(Instant.now())
                .build();
    }

    public static Wallet getWalletMock() {
        return getWalletMock(new BigDecimal("0.00"));
    }
}
