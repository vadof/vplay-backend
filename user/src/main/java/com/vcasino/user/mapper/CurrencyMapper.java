package com.vcasino.user.mapper;

import com.vcasino.user.dto.CurrencyDto;
import com.vcasino.user.entity.Currency;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface CurrencyMapper extends EntityMapper<Currency, CurrencyDto> {
}
