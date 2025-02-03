package com.vcasino.user.mock;

import com.vcasino.user.dto.UserDto;
import com.vcasino.user.entity.Role;
import com.vcasino.user.entity.User;

import java.time.Instant;

public class UserMocks {
    public static UserDto getUserDtoMock() {
        return UserDto.builder()
                .name("John Doe")
                .email("test@gmail.com")
                .password("test1234")
                .username("test")
                .build();
    }

    public static User getUserMock(boolean active) {
        return User.builder()
                .id(1L)
                .name("John Doe")
                .email("test@gmail.com")
                .password("test1234")
                .username("test")
                .active(active)
                .role(Role.USER)
                .registerDate(Instant.now())
                .frozen(false)
                .build();
    }
}
