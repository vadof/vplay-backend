package com.vcasino.clickerdata.repository;

import com.vcasino.clickerdata.entity.AccountClick;
import com.vcasino.clickerdata.entity.key.ClickKey;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccountClickRepository extends JpaRepository<AccountClick, ClickKey> {

}
