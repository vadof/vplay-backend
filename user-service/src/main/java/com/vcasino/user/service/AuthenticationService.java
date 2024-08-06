package com.vcasino.user.service;

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

    @Transactional
    public AuthenticationResponse register(UserDto userDto) {
        validateEmail(userDto.getEmail());
        validateUsername(userDto.getUsername());

        User user = userMapper.toEntity(userDto);

        user.setRegisterDate(LocalDateTime.now());
        user.setModifiedAt(LocalDateTime.now());
        user.setFrozen(false);
        user.setRole(Role.USER);
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        user = userRepository.saveAndFlush(user);

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
        return new TokenRefreshResponse(token, request.getRefreshToken());
    }

    private void validateUsername(String username) {
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
