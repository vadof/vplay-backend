package com.casino.user.mapper;

import com.casino.user.dto.CountryDto;
import com.casino.user.entity.Country;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface CountryMapper extends EntityMapper<Country, CountryDto> {
}
