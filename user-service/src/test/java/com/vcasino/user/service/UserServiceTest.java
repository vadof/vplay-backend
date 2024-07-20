package com.vcasino.user.service;

import com.vcasino.user.dto.CountryDto;
import com.vcasino.user.dto.UserDto;
import com.vcasino.user.entity.Country;
import com.vcasino.user.entity.User;
import com.vcasino.user.mapper.CountryMapper;
import com.vcasino.user.mapper.CountryMapperImpl;
import com.vcasino.user.mapper.UserMapper;
import com.vcasino.user.mapper.UserMapperImpl;
import com.vcasino.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

@SpringBootTest
@AutoConfigureMockMvc
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;
    @Spy
    private UserMapper userMapper = new UserMapperImpl();
    @Mock
    private RestTemplate restTemplate;
    @InjectMocks
    private UserService userService;

    @BeforeEach
    void iniMapperDependencies() {
        CountryMapper countryMapper = new CountryMapperImpl();
        ReflectionTestUtils.setField(userMapper, "countryMapper", countryMapper);
    }

    UserDto getUserDtoMock() {
        return UserDto.builder()
                .firstname("fname")
                .lastname("lname")
                .email("test@gmail.com")
                .country(new CountryDto("EST", "Estonia"))
                .username("test")
                .build();
    }

    User getUserMock() {
        return User.builder()
                .firstname("fname")
                .lastname("lname")
                .email("test@gmail.com")
                .country(new Country("EST", "Estonia"))
                .password("test1234")
                .username("test")
                .build();
    }

}
