package com.vcasino.user.mapper;

import com.vcasino.user.dto.UserDto;
import com.vcasino.user.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE,
        uses = {CountryMapper.class})
public interface UserMapper extends EntityMapper<User, UserDto> {

    @Mapping(target = "firstname", ignore = true)
    @Mapping(target = "lastname", ignore = true)
    @Mapping(target = "username", ignore = true)
    @Mapping(target = "email", ignore = true)
    @Mapping(target = "balance", ignore = true)
    @Mapping(target = "profit", ignore = true)
    void partialUpdate(@MappingTarget User entity, UserDto dto);

}
