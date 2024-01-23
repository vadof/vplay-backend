package com.casino.user.service;

import com.casino.user.dto.ConversionDto;
import com.casino.user.dto.CountryDto;
import com.casino.user.dto.CurrencyDto;
import com.casino.user.dto.UserDto;
import com.casino.user.entity.Country;
import com.casino.user.entity.Currency;
import com.casino.user.entity.User;
import com.casino.user.exceptions.AppException;
import com.casino.user.mapper.*;
import com.casino.user.repository.CurrencyRepository;
import com.casino.user.repository.UserRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@AutoConfigureMockMvc
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;
    @Spy
    private UserMapper userMapper = new UserMapperImpl();
    @Mock
    private CurrencyRepository currencyRepository;
    @Mock
    private RestTemplate restTemplate;
    @InjectMocks
    private UserService userService;

    @BeforeEach
    void iniMapperDependencies() {
        CountryMapper countryMapper = new CountryMapperImpl();
        CurrencyMapper currencyMapper = new CurrencyMapperImpl();
        ReflectionTestUtils.setField(userMapper, "countryMapper", countryMapper);
        ReflectionTestUtils.setField(userMapper, "currencyMapper", currencyMapper);
    }

    UserDto getUserDtoMock() {
        return UserDto.builder()
                .firstname("fname")
                .lastname("lname")
                .email("test@gmail.com")
                .country(new CountryDto(1L, "Estonia"))
                .balance(new BigDecimal("0"))
                .currency(new CurrencyDto(1L, "EUR"))
                .username("test")
                .build();
    }

    User getUserMock() {
        return User.builder()
                .firstname("fname")
                .lastname("lname")
                .email("test@gmail.com")
                .country(new Country(1L, "Estonia"))
                .currency(new Currency(1L, "EUR"))
                .balance(new BigDecimal("0"))
                .password("test1234")
                .username("test")
                .build();
    }

    @Test
    @DisplayName("Deposit Success")
    void depositSuccess() {
        User user = getUserMock();
        Mockito.when(userRepository.findByUsername(user.getUsername())).thenReturn(Optional.of(user));

        BigDecimal value = new BigDecimal("100");
        UserDto result = userService.deposit(value, user.getUsername());

        Assertions.assertThat(result.getBalance()).isEqualTo(value);

        Mockito.verify(userMapper, Mockito.times(1)).toDto(Mockito.any(User.class));
        Mockito.verify(userRepository, Mockito.times(1)).save(Mockito.any(User.class));
    }

    @Test
    @DisplayName("Deposit Negative value Failure")
    void depositNegativeValueFailure() {
        User user = getUserMock();
        Mockito.when(userRepository.findByUsername(user.getUsername())).thenReturn(Optional.of(user));

        BigDecimal value = new BigDecimal("-100");
        assertThrows(AppException.class, () -> userService.deposit(value, user.getUsername()));
    }

    @Test
    @DisplayName("Update User Success")
    void updateSuccess() {
        User user = getUserMock();
        Mockito.when(userRepository.findByUsername(user.getUsername())).thenReturn(Optional.of(user));

        UserDto toUpdate = getUserDtoMock();
        CurrencyDto currencyDto = new CurrencyDto(2L, "USD");
        toUpdate.setCurrency(currencyDto);

        Currency currency = new Currency(2L, "USD");
        Mockito.when(currencyRepository.findByCode(currency.getCode())).thenReturn(Optional.of(currency));

        ConversionDto conversionDto = ConversionDto.builder()
                .result(new BigDecimal("90.32"))
                .build();

        Mockito.when(restTemplate.postForObject(
                Mockito.any(String.class), Mockito.any(ConversionDto.class), Mockito.eq(ConversionDto.class)))
                .thenReturn(conversionDto);

        UserDto result = userService.updateUser(toUpdate, user.getUsername());

        Assertions.assertThat(result.getBalance()).isEqualTo(conversionDto.getResult());
        Mockito.verify(userRepository, Mockito.times(1)).save(Mockito.any(User.class));
        Mockito.verify(userMapper, Mockito.times(1)).toDto(Mockito.any(User.class));
    }

    @Test
    @DisplayName("Update Invalid Currency Failure")
    void updateInvalidCurrencyFailure() {
        User user = getUserMock();
        Mockito.when(userRepository.findByUsername(user.getUsername())).thenReturn(Optional.of(user));

        UserDto toUpdate = getUserDtoMock();
        CurrencyDto currencyDto = new CurrencyDto(2L, "USD");
        toUpdate.setCurrency(currencyDto);

        Currency currency = new Currency(2L, "USDT");
        Mockito.when(currencyRepository.findByCode(currency.getCode())).thenReturn(Optional.empty());

        assertThrows(AppException.class, () -> userService.updateUser(toUpdate, user.getUsername()));
    }

}
