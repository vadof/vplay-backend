package com.vcasino.user.service;

import com.vcasino.user.config.securiy.JwtService;
import com.vcasino.user.dto.AuthenticationRequest;
import com.vcasino.user.dto.AuthenticationResponse;
import com.vcasino.user.dto.CountryDto;
import com.vcasino.user.dto.TokenRefreshRequest;
import com.vcasino.user.dto.TokenRefreshResponse;
import com.vcasino.user.dto.UserDto;
import com.vcasino.user.entity.RefreshToken;
import com.vcasino.user.entity.Role;
import com.vcasino.user.entity.User;
import com.vcasino.user.exception.AppException;
import com.vcasino.user.kafka.producer.UserProducer;
import com.vcasino.user.mapper.UserMapper;
import com.vcasino.user.repository.UserRepository;
import com.vcasino.utils.RegexUtil;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.regex.Pattern;

@Service
@AllArgsConstructor
@Slf4j
public class AuthenticationService {

    private final UserRepository userRepository;
    private final UserProducer userProducer;
    private final UserMapper userMapper;

    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    private final RefreshTokenService refreshTokenService;
    private final CountryService countryService;

    public List<CountryDto> getCountries() {
        return countryService.getCountries();
    }

    public AuthenticationResponse register(UserDto userDto, Role role) {
        validateEmail(userDto.getEmail());
        validateUsername(userDto.getUsername());

        if (role.equals(Role.ADMIN)) {
            validatePassword(userDto.getPassword());
        }

        User user = userMapper.toEntity(userDto);
        user.setRegisterDate(LocalDateTime.now());
        user.setModifiedAt(LocalDateTime.now());
        user.setFrozen(false);
        user.setRole(role);
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        user = userRepository.save(user);

        String jwtToken = jwtService.generateToken(user);

        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user.getId());

        log.info(String.format("User with username \"%s\" saved to database", user.getUsername()));

        userProducer.sendUserCreated(user.getId());

        return new AuthenticationResponse(jwtToken, refreshToken.getToken(), userMapper.toDto(user));
    }

    @Transactional
    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));

        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new AppException("Unauthorized access", HttpStatus.FORBIDDEN));

        String jwtToken = jwtService.generateToken(user);
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user.getId());

        return new AuthenticationResponse(jwtToken, refreshToken.getToken(), userMapper.toDto(user));
    }

    @Transactional
    public TokenRefreshResponse refreshToken(TokenRefreshRequest request) {
        RefreshToken refreshToken = refreshTokenService.findByToken(request.getRefreshToken());
        refreshTokenService.verifyExpiration(refreshToken);
        String token = jwtService.generateToken(refreshToken.getUser());
        return new TokenRefreshResponse(token);
    }

    private void validateUsername(String username) {
        if (!username.matches(RegexUtil.USERNAME_REGEX)) {
            throw new AppException("Username can only contain english letters, numbers and underscores", HttpStatus.BAD_REQUEST);
        }

        if (userRepository.findByUsername(username).isPresent()) {
            throw new AppException("Username already exists", HttpStatus.BAD_REQUEST);
        }
    }

    private void validateEmail(String email) {
        if (userRepository.findByEmail(email).isPresent()) {
            throw new AppException("Email already in use", HttpStatus.BAD_REQUEST);
        }
    }

    private void validatePassword(String password) {
        if (password.length() < 12) {
            throw new AppException("Min password length is 12 symbols", HttpStatus.BAD_REQUEST);
        }

        String invalidChars = getInvalidCharacters(password);
        if (!invalidChars.isEmpty()) {
            throw new AppException("Password cannot contain: " + invalidChars, HttpStatus.BAD_REQUEST);
        }

        if (!passwordContainsSpecialSymbol(password)) {
            throw new AppException("Password must contain at least 1 special symbol", HttpStatus.BAD_REQUEST);
        }

        if (!passwordContainsCapitalLetter(password)) {
            throw new AppException("Password must contain at least 1 capital letter", HttpStatus.BAD_REQUEST);
        }

        if (!passwordContainsNumber(password)) {
            throw new AppException("Password must contain at least 1 number", HttpStatus.BAD_REQUEST);
        }
    }

    private String getInvalidCharacters(String password) {
        StringBuilder invalidChars = new StringBuilder();
        for (char ch : password.toCharArray()) {
            if (!String.valueOf(ch).matches(RegexUtil.PASSWORD_REGEX)) {
                invalidChars.append(ch);
            }
        }
        return invalidChars.toString();
    }

    private boolean passwordContainsSpecialSymbol(String password) {
        Pattern r = Pattern.compile(RegexUtil.SPECIAL_SYMBOL_REGEX);
        return r.matcher(password).find();
    }

    private boolean passwordContainsCapitalLetter(String password) {
        return password.matches(".*" + RegexUtil.CAPITAL_LETTER_REGEX + ".*");
    }

    private boolean passwordContainsNumber(String password) {
        return password.matches(".*" + RegexUtil.NUMBER_REGEX + ".*");
    }
}
