package com.casino.user.mapper;

import com.casino.user.dto.CurrencyDto;
import com.casino.user.entity.Currency;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface CurrencyMapper extends EntityMapper<Currency, CurrencyDto> {
}
