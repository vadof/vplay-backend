package com.vcasino.authentication.service;

import com.vcasino.authentication.dto.AuthenticationRequest;
import com.vcasino.authentication.dto.AuthenticationResponse;
import com.vcasino.authentication.dto.TokenRefreshRequest;
import com.vcasino.authentication.dto.TokenRefreshResponse;
import com.vcasino.authentication.dto.UserDto;
import com.vcasino.authentication.entity.RefreshToken;
import com.vcasino.authentication.entity.User;
import com.vcasino.authentication.exception.AppException;
import com.vcasino.authentication.mapper.CountryMapper;
import com.vcasino.authentication.mapper.CountryMapperImpl;
import com.vcasino.authentication.mapper.UserMapper;
import com.vcasino.authentication.mapper.UserMapperImpl;
import com.vcasino.authentication.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.Optional;

import static com.vcasino.authentication.mock.RefreshTokenMocks.getRefreshTokenMock;
import static com.vcasino.authentication.mock.UserMocks.getUserDtoMock;
import static com.vcasino.authentication.mock.UserMocks.getUserMock;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** UNIT tests for {@link AuthenticationService} */

@ExtendWith(MockitoExtension.class)
public class AuthenticationServiceTests {

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private UserRepository userRepository;

    @Mock
    private JwtService jwtService;

    @Spy
    private UserMapper userMapper = new UserMapperImpl();

    @InjectMocks
    private AuthenticationService authenticationService;

    @Captor
    private ArgumentCaptor<User> userArgumentCaptor;

    @Mock
    private RefreshTokenService refreshTokenService;

    @BeforeEach
    void iniMapperDependencies() {
        CountryMapper countryMapper = new CountryMapperImpl();
        ReflectionTestUtils.setField(userMapper, "countryMapper", countryMapper);
    }

    @Test
    @DisplayName("Register user")
    void register() {
        UserDto toSave = getUserDtoMock();

        RefreshToken refreshToken = getRefreshTokenMock();
        when(refreshTokenService.createRefreshToken(any())).thenReturn(refreshToken);
        when(jwtService.generateToken(any())).thenReturn("token");

        AuthenticationResponse response = authenticationService.register(toSave);

        verify(userRepository, times(1)).save(userArgumentCaptor.capture());

        User saved = userArgumentCaptor.getValue();

        verify(userMapper, times(1)).toEntity(toSave);
        verify(userMapper, times(1)).toDto(saved);
        verify(userRepository, times(1)).save(saved);
        verify(jwtService, times(1)).generateToken(saved);
        verify(refreshTokenService, times(1)).createRefreshToken(any());

        assertEquals(toSave.getEmail(), response.getUser().getEmail());
        assertEquals(toSave.getUsername(), response.getUser().getUsername());
        assertEquals(toSave.getCountry().getCode(), response.getUser().getCountry().getCode());
        assertNotNull(response.getRefreshToken());
        assertNotNull(response.getToken());
    }

    @Test
    @DisplayName("Register user with already existing username")
    void registerUsernameExists() {
        UserDto toSave = getUserDtoMock();

        when(userRepository.findByUsername(toSave.getUsername())).thenReturn(Optional.of(getUserMock()));

        AppException exception = assertThrows(AppException.class, () -> authenticationService.register(toSave));

        assertTrue(exception.getMessage().contains("Username"));
        assertEquals(HttpStatus.BAD_REQUEST, exception.getHttpStatus());
    }

    @Test
    @DisplayName("Register user with existing email")
    void registerEmailExists() {
        UserDto toSave = getUserDtoMock();

        when(userRepository.findByEmail(toSave.getEmail())).thenReturn(Optional.of(getUserMock()));

        AppException exception = assertThrows(AppException.class, () -> authenticationService.register(toSave));

        assertTrue(exception.getMessage().contains("Email"));
        assertEquals(HttpStatus.BAD_REQUEST, exception.getHttpStatus());
    }

    @Test
    @DisplayName("Authenticate user")
    void authenticate() {
        UserDto userDto = getUserDtoMock();
        User user = getUserMock();
        RefreshToken refreshToken = getRefreshTokenMock();

        AuthenticationRequest request = new AuthenticationRequest(userDto.getUsername(), userDto.getPassword());
        when(authenticationManager.authenticate(any())).thenReturn(any());

        when(userRepository.findByUsername(request.getUsername())).thenReturn(Optional.of(user));
        when(jwtService.generateToken(user)).thenReturn("token");
        when(refreshTokenService.createRefreshToken(user.getId())).thenReturn(refreshToken);

        AuthenticationResponse response = authenticationService.authenticate(request);

        assertEquals("token", response.getToken());
        assertEquals(user.getUsername(), response.getUser().getUsername());
        assertEquals(refreshToken.getToken(), response.getRefreshToken());
    }

    @Test
    @DisplayName("Refresh token")
    void refreshToken() {
        RefreshToken refreshToken = getRefreshTokenMock();
        refreshToken.setExpiryDate(Instant.now().plusSeconds(60));

        when(refreshTokenService.findByToken(refreshToken.getToken())).thenReturn(refreshToken);
        when(jwtService.generateToken(any())).thenReturn("token");

        TokenRefreshResponse response = authenticationService.refreshToken(new TokenRefreshRequest(refreshToken.getToken()));

        assertEquals("token", response.getToken());
        assertEquals(refreshToken.getToken(), response.getRefreshToken());
    }

    @Test
    @DisplayName("Refresh token expired")
    void refreshExpiredToken() {
        RefreshToken refreshToken = getRefreshTokenMock();

        when(refreshTokenService.findByToken(refreshToken.getToken())).thenReturn(refreshToken);
        when(refreshTokenService.verifyExpiration(refreshToken)).thenThrow(
                new AppException("Refresh token was expired. Please make a new login request", HttpStatus.UNAUTHORIZED));

        AppException exception = assertThrows(AppException.class,
                () -> authenticationService.refreshToken(new TokenRefreshRequest(refreshToken.getToken())));

        assertEquals("Refresh token was expired. Please make a new login request", exception.getMessage());
        assertEquals( HttpStatus.UNAUTHORIZED, exception.getHttpStatus());
    }
}
