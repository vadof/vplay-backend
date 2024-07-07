package com.vcasino.user.service;

import com.vcasino.user.dto.ConversionDto;
import com.vcasino.user.dto.UserDto;
import com.vcasino.user.entity.Currency;
import com.vcasino.user.entity.User;
import com.vcasino.user.exceptions.AppException;
import com.vcasino.user.mapper.UserMapper;
import com.vcasino.user.repository.CurrencyRepository;
import com.vcasino.user.repository.UserRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;

@Service
@AllArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final CurrencyRepository currencyRepository;
    private final RestTemplate restTemplate;

    // TODO send log
    @Transactional
    public UserDto deposit(BigDecimal value, String username) {
        if (value.compareTo(new BigDecimal(0)) <= 0) {
            throw new AppException("Value should be a positive number", HttpStatus.BAD_REQUEST);
        }

        User user = getUserByUsername(username);
        user.setBalance(user.getBalance().add(value));
        userRepository.save(user);
        return userMapper.toDto(user);
    }

    @Transactional
    public UserDto updateUser(UserDto userDto, String username) {
        User user = getUserByUsername(username);

        Currency currency = currencyRepository.findByCode(userDto.getCurrency().getCode())
                .orElseThrow(() -> new AppException("Currency not found", HttpStatus.NOT_FOUND));

        if (!user.getCurrency().equals(currency)) {
            changeUserCurrency(user, currency);
        }

        userMapper.partialUpdate(user, userDto);
        user.setCurrency(currency);

        userRepository.save(user);

        return userMapper.toDto(user);
    }

    public UserDto findById(Long id) {
        return userMapper.toDto(getUserById(id));
    }

    private User getUserById(Long id) {
        return userRepository.findById(id).orElseThrow(
                () -> new AppException("User#" + id + " not found", HttpStatus.NOT_FOUND));
    }

    private User getUserByUsername(String username) {
        return userRepository.findByUsername(username).orElseThrow(
                () -> new AppException("User#" + username + " not found", HttpStatus.NOT_FOUND));
    }

    private void changeUserCurrency(User user, Currency currency) {
        try {
            ConversionDto conversionDto = ConversionDto.builder()
                    .baseCode(user.getCurrency().getCode())
                    .toBaseCode(currency.getCode())
                    .amount(user.getBalance())
                    .build();

            conversionDto = restTemplate.postForObject(
                    "http://finance-service/api/v1/finances/convert", conversionDto, ConversionDto.class);

            user.setBalance(conversionDto.getResult());
        } catch (Exception e) {
            throw new AppException("Error during currency conversion", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
