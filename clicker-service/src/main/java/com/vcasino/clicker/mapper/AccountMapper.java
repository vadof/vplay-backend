package com.vcasino.clicker.mapper;

import com.vcasino.clicker.dto.AccountDto;
import com.vcasino.clicker.entity.Account;
import com.vcasino.clicker.mapper.common.EntityMapper;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE,
        uses = {UpgradeMapper.class, SectionMapper.class, ConditionMapper.class})
public interface AccountMapper extends EntityMapper<Account, AccountDto> {
}
