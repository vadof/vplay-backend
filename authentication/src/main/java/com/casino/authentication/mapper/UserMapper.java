package com.casino.authentication.mapper;

import com.casino.authentication.dto.UserDto;
import com.casino.authentication.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE,
        uses = {CurrencyMapper.class, CountryMapper.class})
public interface UserMapper extends EntityMapper<User, UserDto> {
}
