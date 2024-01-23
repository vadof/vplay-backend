package com.casino.authentication.service;

import com.casino.authentication.dto.*;
import com.casino.authentication.entity.RefreshToken;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.casino.authentication.entity.Role;
import com.casino.authentication.entity.User;
import com.casino.authentication.exceptions.AppException;
import com.casino.authentication.mapper.UserMapper;
import com.casino.authentication.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

@Service
@AllArgsConstructor
@Slf4j
public class AuthenticationService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final UserMapper userMapper;
    private final RefreshTokenService refreshTokenService;

    @Transactional
    public AuthenticationResponse register(UserDto userDto) {
        validateEmail(userDto.getEmail());
        validateUniqueUsername(userDto.getUsername());

        User user = userMapper.toEntity(userDto);

        user.setRegisterDate(LocalDate.now());
        user.setBalance(new BigDecimal(0));
        user.setProfit(new BigDecimal(0));
        user.setRole(Role.USER);
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        userRepository.save(user);

        String jwtToken = jwtService.generateToken(user);

        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user.getId());

        log.info(String.format("User with username \"%s\" saved to database", user.getUsername()));

        return new AuthenticationResponse(jwtToken, refreshToken.getToken(), userMapper.toDto(user));
    }

    @Transactional
    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));

        User user = userRepository.findByUsername(request.getUsername()).orElseThrow();
        String jwtToken = jwtService.generateToken(user);
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user.getId());

        return new AuthenticationResponse(jwtToken, refreshToken.getToken(), userMapper.toDto(user));
    }

    @Transactional
    public TokenRefreshResponse refreshToken(TokenRefreshRequest request) {
        RefreshToken refreshToken = refreshTokenService.findByToken(request.getRefreshToken());
        refreshTokenService.verifyExpiration(refreshToken);
        String token = jwtService.generateToken(refreshToken.getUser());
        return new TokenRefreshResponse(token, request.getRefreshToken());
    }

    private void validateUniqueUsername(String username) {
        if (userRepository.findByUsername(username).isPresent()) {
            throw new AppException("Username already exists", HttpStatus.BAD_REQUEST);
        }
    }

    private void validateEmail(String email) {
        if (userRepository.findByEmail(email).isPresent()) {
            throw new AppException("Email already in use", HttpStatus.BAD_REQUEST);
        }
    }
}
