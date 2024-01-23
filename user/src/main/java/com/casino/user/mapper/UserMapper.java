package com.casino.user.mapper;

import com.casino.user.dto.UserDto;
import com.casino.user.entity.User;
import org.mapstruct.*;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE,
        uses = {CurrencyMapper.class, CountryMapper.class})
public interface UserMapper extends EntityMapper<User, UserDto> {

    @Mapping(target = "firstname", ignore = true)
    @Mapping(target = "lastname", ignore = true)
    @Mapping(target = "username", ignore = true)
    @Mapping(target = "email", ignore = true)
    @Mapping(target = "balance", ignore = true)
    @Mapping(target = "profit", ignore = true)
    void partialUpdate(@MappingTarget User entity, UserDto dto);

}
