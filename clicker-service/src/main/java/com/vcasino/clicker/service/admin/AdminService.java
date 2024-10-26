package com.vcasino.clicker.service.admin;

import com.vcasino.clicker.dto.admin.AccountImprove;
import com.vcasino.clicker.entity.Account;
import com.vcasino.clicker.repository.AccountRepository;
import com.vcasino.clicker.service.AccountService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
@Slf4j
public class AdminService {

    private final AccountService accountService;
    private final AccountRepository accountRepository;

    public Account improveAccount(AccountImprove accountImprove) {
        Account account = accountService.getById(accountImprove.getAccountId());
        accountService.addCoins(account, accountImprove.getAddCoins());
        account = accountRepository.save(account);
        return account;
    }

}
