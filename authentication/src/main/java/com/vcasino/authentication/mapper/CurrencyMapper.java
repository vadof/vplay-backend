package com.vcasino.authentication.mapper;

import com.vcasino.authentication.dto.CurrencyDto;
import com.vcasino.authentication.entity.Currency;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface CurrencyMapper extends EntityMapper<Currency, CurrencyDto> {
}
