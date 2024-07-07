package com.vcasino.authentication.mapper;

import com.vcasino.authentication.dto.UserDto;
import com.vcasino.authentication.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE,
        uses = {CurrencyMapper.class, CountryMapper.class})
public interface UserMapper extends EntityMapper<User, UserDto> {
}
