package com.casino.authentication.mapper;

import com.casino.authentication.dto.CountryDto;
import com.casino.authentication.entity.Country;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface CountryMapper extends EntityMapper<Country, CountryDto> {
}
