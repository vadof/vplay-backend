package com.vcasino.authentication.mapper;

import com.vcasino.authentication.dto.UserDto;
import com.vcasino.authentication.entity.User;
import com.vcasino.mapper.CountryMapper;
import com.vcasino.mapper.EntityMapper;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE,
        uses = {CountryMapper.class})
public interface UserMapper extends EntityMapper<User, UserDto> {
}
