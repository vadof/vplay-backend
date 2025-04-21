package com.vcasino.wallet.repository;

import com.vcasino.wallet.entity.ReferralBonus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ReferralBonusRepository extends JpaRepository<ReferralBonus, Long> {
    Optional<ReferralBonus> findByWalletId(Long walletId);
}
