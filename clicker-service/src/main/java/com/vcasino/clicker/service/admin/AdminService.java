package com.vcasino.clicker.service.admin;

import com.vcasino.clicker.dto.AccountDto;
import com.vcasino.clicker.dto.admin.AccountImprove;
import com.vcasino.clicker.dto.admin.FrozenStatus;
import com.vcasino.clicker.entity.Account;
import com.vcasino.clicker.service.AccountService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
@Slf4j
public class AdminService {

    private final AccountService accountService;

    public AccountDto improveAccount(AccountImprove accountImprove) {
        Account account = accountService.getByIdForce(accountImprove.getAccountId());
        accountService.addCoins(account, accountImprove.getAddCoins());
        account = accountService.save(account);
        return accountService.toDto(account);
    }

    public void changeFrozenStatus(FrozenStatus frozenStatus) {
        Account account = accountService.getByIdForce(frozenStatus.getAccountId());
        account.setFrozen(frozenStatus.getStatus());
        accountService.save(account);
    }
}
