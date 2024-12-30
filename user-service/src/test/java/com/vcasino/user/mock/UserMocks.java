package com.vcasino.user.mock;

import com.vcasino.user.dto.UserDto;
import com.vcasino.user.entity.User;

public class UserMocks {
    public static UserDto getUserDtoMock() {
        return UserDto.builder()
                .name("John Doe")
                .email("test@gmail.com")
                .password("test1234")
                .username("test")
                .build();
    }

    public static User getUserMock() {
        return User.builder()
                .name("John Doe")
                .email("test@gmail.com")
                .password("test1234")
                .username("test")
                .build();
    }
}
