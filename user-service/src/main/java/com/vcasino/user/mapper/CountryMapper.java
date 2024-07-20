package com.vcasino.user.mapper;

import com.vcasino.user.dto.CountryDto;
import com.vcasino.user.entity.Country;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface CountryMapper extends EntityMapper<Country, CountryDto> {
}