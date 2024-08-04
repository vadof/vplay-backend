package com.vcasino.clicker.repository;

import com.vcasino.clicker.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccountRepository extends JpaRepository<Account, Long> {

}
