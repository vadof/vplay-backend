package com.vcasino.wallet.service;

import com.vcasino.wallet.dto.ReferralBonusDto;
import com.vcasino.wallet.entity.ReferralBonus;
import com.vcasino.wallet.entity.Wallet;
import com.vcasino.wallet.repository.ReferralBonusRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Optional;

@Service
@AllArgsConstructor
@Slf4j
public class ReferralBonusService {

    private final ReferralBonusRepository referralBonusRepository;
    private final WalletService walletService;

    public void addReferralBonus(ReferralBonusDto referralBonus) {
        Optional<ReferralBonus> existingBonus = referralBonusRepository.findByWalletId(referralBonus.getReferralId());

        BigDecimal amount = referralBonus.getBonusAmount().setScale(2, RoundingMode.DOWN);
        if (existingBonus.isPresent()) {
            ReferralBonus bonus = existingBonus.get();
            if (amount.compareTo(BigDecimal.ZERO) == 0) {
                referralBonusRepository.delete(bonus);
            } else {
                bonus.setAmount(amount);
                referralBonusRepository.save(bonus);
            }
        } else {
            Wallet wallet = walletService.getById(referralBonus.getReferralId());
            referralBonusRepository.save(new ReferralBonus(null, amount, wallet));
        }

    }

}
