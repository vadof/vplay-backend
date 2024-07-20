package com.vcasino.authentication.mock;

import com.vcasino.authentication.dto.UserDto;
import com.vcasino.authentication.entity.User;

public class UserMocks {
    public static UserDto getUserDtoMock() {
        return UserDto.builder()
                .firstname("John")
                .lastname("Doe")
                .email("test@gmail.com")
                .country(CountryMocks.getCountryDtoMock())
                .password("test1234")
                .username("test")
                .build();
    }

    public static User getUserMock() {
        return User.builder()
                .firstname("John")
                .lastname("Doe")
                .email("test@gmail.com")
                .country(CountryMocks.getCountryMock())
                .password("test1234")
                .username("test")
                .build();
    }
}
