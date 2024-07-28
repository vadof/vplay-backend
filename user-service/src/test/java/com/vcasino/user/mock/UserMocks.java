package com.vcasino.user.mock;

import com.vcasino.user.dto.UserDto;
import com.vcasino.user.entity.User;

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
