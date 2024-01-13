package com.casino.authentication.mapper;

import com.casino.authentication.dto.CurrencyDto;
import com.casino.authentication.entity.Currency;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface CurrencyMapper extends EntityMapper<Currency, CurrencyDto> {
}
