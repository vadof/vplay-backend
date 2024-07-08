package com.vcasino.authentication.service;

import com.vcasino.authentication.dto.*;
import com.vcasino.authentication.entity.Country;
import com.vcasino.authentication.entity.User;
import com.vcasino.authentication.exception.AppException;
import com.vcasino.authentication.mapper.*;
import com.vcasino.authentication.repository.UserRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.Assert.assertThrows;

@SpringBootTest
@AutoConfigureMockMvc
public class AuthenticationServiceTests {
    @Mock
    private UserRepository userRepository;

    @Mock
    private JwtService jwtService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Spy
    private UserMapper userMapper = new UserMapperImpl();

    @InjectMocks
    private AuthenticationService authenticationService;

    @Captor
    private ArgumentCaptor<User> userArgumentCaptor;

    @Mock
    private AuthenticationManager authenticationManager;

//    UserDto getUserDtoMock() {
//        return UserDto.builder()
//                .firstname("fname")
//                .lastname("lname")
//                .email("test@gmail.com")
//                .country(new CountryDto(1L, "Estonia"))
//                .currency(new CurrencyDto(1L, "EUR"))
//                .password("test1234")
//                .username("test")
//                .build();
//    }
//
//    User getUserMock() {
//        return User.builder()
//                .firstname("fname")
//                .lastname("lname")
//                .email("test@gmail.com")
//                .country(new Country(1L, "Estonia"))
//                .currency(new Currency(1L, "EUR"))
//                .password("test1234")
//                .username("test")
//                .build();
//    }
//
//    @BeforeEach
//    void iniMapperDependencies() {
//        CountryMapper countryMapper = new CountryMapperImpl();
//        CurrencyMapper currencyMapper = new CurrencyMapperImpl();
//        ReflectionTestUtils.setField(userMapper, "countryMapper", countryMapper);
//        ReflectionTestUtils.setField(userMapper, "currencyMapper", currencyMapper);
//    }
//
//    @Test
//    @DisplayName("Register user - Success")
//    void registerSuccess() {
//        UserDto toSave = getUserDtoMock();
//
//        AuthenticationResponse response = authenticationService.register(toSave);
//
//        Mockito.verify(userRepository, Mockito.times(1)).save(userArgumentCaptor.capture());
//        User saved = userArgumentCaptor.getValue();
//
//        Mockito.verify(userMapper, Mockito.times(1)).toEntity(toSave);
//        Mockito.verify(userMapper, Mockito.times(1)).toDto(saved);
//
//        Assertions.assertThat(response.getUser().getEmail()).isEqualTo(toSave.getEmail());
//        Assertions.assertThat(response.getUser().getUsername()).isEqualTo(toSave.getUsername());
//        Assertions.assertThat(response.getUser().getBalance()).isEqualTo(new BigDecimal(0));
//    }
//
//    @Test
//    @DisplayName("Register user | Username Exists - Failure")
//    void registerUsernameFailure() {
//        UserDto toSave = getUserDtoMock();
//
//        Mockito.when(userRepository.findByUsername(toSave.getUsername())).thenReturn(Optional.of(getUserMock()));
//
//        AppException exception = assertThrows(
//                AppException.class,
//                () -> authenticationService.register(toSave)
//        );
//
//        Assertions.assertThat(exception.getMessage()).contains("Username");
//        Assertions.assertThat(exception.getHttpStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
//    }
//
//    @Test
//    @DisplayName("Register user | Email Exists - Failure")
//    void registerEmailFailure() {
//        UserDto toSave = getUserDtoMock();
//
//        Mockito.when(userRepository.findByEmail(toSave.getEmail())).thenReturn(Optional.of(getUserMock()));
//
//        AppException exception = assertThrows(
//                AppException.class,
//                () -> authenticationService.register(toSave)
//        );
//
//        Assertions.assertThat(exception.getMessage()).contains("Email");
//        Assertions.assertThat(exception.getHttpStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
//    }
//
//    @Test
//    @DisplayName("Authenticate user Success")
//    void authenticateSuccess() {
//        UserDto userDto = getUserDtoMock();
//        User user = getUserMock();
//
//        AuthenticationRequest ar = new AuthenticationRequest(userDto.getUsername(), userDto.getPassword());
//
//        Mockito.when(userRepository.findByUsername(ar.getUsername())).thenReturn(Optional.of(user));
//        Mockito.when(jwtService.generateToken(user)).thenReturn("token");
//
//        AuthenticationResponse response = authenticationService.authenticate(ar);
//
//        Assertions.assertThat(response.getToken()).isEqualTo("token");
//        Assertions.assertThat(response.getUser().getUsername()).isEqualTo(user.getUsername());
//    }
}
