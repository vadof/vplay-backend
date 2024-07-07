package com.vcasino.authentication.mapper;

import com.vcasino.authentication.dto.CountryDto;
import com.vcasino.authentication.entity.Country;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface CountryMapper extends EntityMapper<Country, CountryDto> {
}
