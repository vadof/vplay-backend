package com.vcasino.user.service;

import com.vcasino.user.config.ApplicationConfig;
import com.vcasino.user.config.securiy.JwtService;
import com.vcasino.user.dto.AuthenticationRequest;
import com.vcasino.user.dto.AuthenticationResponse;
import com.vcasino.user.dto.TokenRefreshRequest;
import com.vcasino.user.dto.TokenRefreshResponse;
import com.vcasino.user.dto.UserDto;
import com.vcasino.user.entity.Role;
import com.vcasino.user.entity.Token;
import com.vcasino.user.entity.TokenType;
import com.vcasino.user.entity.User;
import com.vcasino.user.exception.AppException;
import com.vcasino.user.kafka.producer.UserProducer;
import com.vcasino.user.mapper.UserMapper;
import com.vcasino.user.mapper.UserMapperImpl;
import com.vcasino.user.repository.UserRepository;
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
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.lang.reflect.Field;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static com.vcasino.user.mock.TokenMocks.getConfirmationTokenMock;
import static com.vcasino.user.mock.TokenMocks.getRefreshTokenMock;
import static com.vcasino.user.mock.UserMocks.getUserDtoMock;
import static com.vcasino.user.mock.UserMocks.getUserMock;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * UNIT tests for {@link AuthenticationService}
 */
@ExtendWith(MockitoExtension.class)
public class AuthenticationServiceTests {

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserProducer userProducer;

    @Mock
    private JwtService jwtService;

    @Spy
    private UserMapper userMapper = new UserMapperImpl();

    @InjectMocks
    private AuthenticationService authenticationService;

    @Captor
    private ArgumentCaptor<User> userArgumentCaptor;

    @Mock
    private TokenService tokenService;

    @Mock
    private CookieService cookieService;

    private ApplicationConfig applicationConfig;

    @BeforeEach
    void initConfig() throws Exception {
        ApplicationConfig config = new ApplicationConfig();
        Field field = AuthenticationService.class.getDeclaredField("applicationConfig");
        field.setAccessible(true);
        field.set(authenticationService, config);
        this.applicationConfig = config;
    }


    @Test
    @DisplayName("Register user")
    void register() {
        applicationConfig.setProduction(false);

        UserDto toSave = getUserDtoMock();

        Token refreshToken = getRefreshTokenMock();
        when(tokenService.createToken(any(), eq(TokenType.REFRESH))).thenReturn(refreshToken);
        when(jwtService.generateToken(any())).thenReturn("token");

        User userMock = getUserMock();
        userMock.setRole(Role.USER);
        when(userRepository.save(any())).thenReturn(userMock);

        AuthenticationResponse response = authenticationService.register(toSave, Role.USER);

        verify(userRepository, times(1)).save(userArgumentCaptor.capture());

        User saved = userArgumentCaptor.getValue();

        verify(userMapper, times(1)).toEntity(toSave);
        verify(userMapper, times(1)).toDto(saved);
        verify(userRepository, times(1)).save(saved);
        verify(jwtService, times(1)).generateToken(saved);
        verify(tokenService, times(1)).createToken(any(), eq(TokenType.REFRESH));
        verify(userProducer, times(1)).sendUserCreated(saved.getId());

        assertEquals(userMock.getRole(), saved.getRole());
        assertEquals(toSave.getEmail(), response.getUser().getEmail());
        assertTrue(saved.getActive());
        assertFalse(saved.getFrozen());
        assertEquals(toSave.getUsername(), response.getUser().getUsername());
        assertEquals(toSave.getName(), response.getUser().getName());
        assertNotNull(response.getRefreshToken());
        assertNotNull(response.getToken());
    }

    @Test
    @DisplayName("Register admin")
    void registerAdmin() {
        applicationConfig.setProduction(true);
        String strongPassword = "!Password123";

        UserDto toSave = getUserDtoMock();
        toSave.setPassword(strongPassword);

        Token refreshToken = getRefreshTokenMock();
        when(tokenService.createToken(any(), eq(TokenType.REFRESH))).thenReturn(refreshToken);
        when(jwtService.generateToken(any())).thenReturn("token");

        User adminMock = getUserMock();
        adminMock.setPassword(strongPassword);
        adminMock.setRole(Role.ADMIN);
        when(userRepository.save(any())).thenReturn(adminMock);

        AuthenticationResponse response = authenticationService.register(toSave, Role.ADMIN);

        verify(userRepository, times(1)).save(userArgumentCaptor.capture());

        User saved = userArgumentCaptor.getValue();

        verify(userMapper, times(1)).toEntity(toSave);
        verify(userMapper, times(1)).toDto(saved);
        verify(userRepository, times(1)).save(saved);
        verify(jwtService, times(1)).generateToken(saved);
        verify(tokenService, times(1)).createToken(any(), eq(TokenType.REFRESH));
        verify(userProducer, times(1)).sendUserCreated(saved.getId());

        assertEquals(adminMock.getRole(), saved.getRole());
        assertEquals(adminMock.getName(), saved.getName());
        assertTrue(adminMock.getActive());
        assertFalse(adminMock.getFrozen());
        assertEquals(toSave.getEmail(), response.getUser().getEmail());
        assertEquals(toSave.getUsername(), response.getUser().getUsername());
        assertNotNull(response.getRefreshToken());
        assertNotNull(response.getToken());
    }

    @Test
    @DisplayName("Register user email already in use by active user")
    void registerEmailInUseByActiveUser() {
        UserDto toSave = getUserDtoMock();
        User existingUser = getUserMock();
        existingUser.setActive(true);

        when(userRepository.findByEmail(toSave.getEmail())).thenReturn(Optional.of(existingUser));

        AppException exception = assertThrows(AppException.class, () -> authenticationService.register(toSave, Role.USER));

        assertTrue(exception.getMessage().contains("Email"));
        assertEquals(HttpStatus.BAD_REQUEST, exception.getHttpStatus());
    }

    @Test
    @DisplayName("Register user email already in use by inactive user")
    void registerEmailInUseByInactiveUser() {
        LocalDateTime now = LocalDateTime.now();

        long tokenExpirationInMinutes = 10;
        ApplicationConfig.ConfirmationProperties confirmation = new ApplicationConfig.ConfirmationProperties();
        ApplicationConfig.ConfirmationProperties.TokenProperties tokenProps = new ApplicationConfig.ConfirmationProperties.TokenProperties();
        tokenProps.setExpirationMs(tokenExpirationInMinutes * 60 * 1000);
        confirmation.setToken(tokenProps);
        applicationConfig.setConfirmation(confirmation);

        UserDto toSave = getUserDtoMock();

        User existingUser = getUserMock();
        existingUser.setActive(false);
        existingUser.setRegisterDate(now.minusMinutes(1));

        when(userRepository.findByEmail(toSave.getEmail())).thenReturn(Optional.of(existingUser));

        AppException exception = assertThrows(AppException.class, () -> authenticationService.register(toSave, Role.USER));

        assertTrue(exception.getMessage().contains("Email"));
        assertEquals(HttpStatus.BAD_REQUEST, exception.getHttpStatus());

        existingUser.setRegisterDate(now.minusMinutes(tokenExpirationInMinutes * 2));

        applicationConfig.setProduction(false);
        Token refreshToken = getRefreshTokenMock();
        when(tokenService.createToken(any(), eq(TokenType.REFRESH))).thenReturn(refreshToken);
        when(jwtService.generateToken(any())).thenReturn("token");

        User userMock = getUserMock();
        userMock.setRole(Role.USER);
        when(userRepository.save(any())).thenReturn(userMock);

        AuthenticationResponse response = authenticationService.register(toSave, Role.USER);
        verify(userRepository, times(1)).delete(existingUser);
        verify(userRepository, times(1)).save(userMock);

        assertNotNull(response);
    }

    @Test
    @DisplayName("Register user username already in use by active user")
    void registerUsernameInUseByActiveUser() {
        UserDto toSave = getUserDtoMock();
        User existingUser = getUserMock();
        existingUser.setActive(true);

        when(userRepository.findByUsername(toSave.getUsername())).thenReturn(Optional.of(existingUser));

        AppException exception = assertThrows(AppException.class, () -> authenticationService.register(toSave, Role.USER));

        assertTrue(exception.getMessage().contains("Username"));
        assertEquals(HttpStatus.BAD_REQUEST, exception.getHttpStatus());
    }

    @Test
    @DisplayName("Register user username already in use by inactive user")
    void registerUsernameInUseByInactiveUser() {
        LocalDateTime now = LocalDateTime.now();

        long tokenExpirationInMinutes = 10;
        ApplicationConfig.ConfirmationProperties confirmation = new ApplicationConfig.ConfirmationProperties();
        ApplicationConfig.ConfirmationProperties.TokenProperties tokenProps = new ApplicationConfig.ConfirmationProperties.TokenProperties();
        tokenProps.setExpirationMs(tokenExpirationInMinutes * 60 * 1000);
        confirmation.setToken(tokenProps);
        applicationConfig.setConfirmation(confirmation);

        UserDto toSave = getUserDtoMock();

        User existingUser = getUserMock();
        existingUser.setActive(false);
        existingUser.setRegisterDate(now.minusMinutes(1));

        when(userRepository.findByUsername(toSave.getUsername())).thenReturn(Optional.of(existingUser));

        AppException exception = assertThrows(AppException.class, () -> authenticationService.register(toSave, Role.USER));

        assertTrue(exception.getMessage().contains("Username"));
        assertEquals(HttpStatus.BAD_REQUEST, exception.getHttpStatus());

        existingUser.setRegisterDate(now.minusMinutes(tokenExpirationInMinutes * 2));

        applicationConfig.setProduction(false);
        Token refreshToken = getRefreshTokenMock();
        when(tokenService.createToken(any(), eq(TokenType.REFRESH))).thenReturn(refreshToken);
        when(jwtService.generateToken(any())).thenReturn("token");

        User userMock = getUserMock();
        userMock.setRole(Role.USER);
        when(userRepository.save(any())).thenReturn(userMock);

        AuthenticationResponse response = authenticationService.register(toSave, Role.USER);
        verify(userRepository, times(1)).delete(existingUser);
        verify(userRepository, times(1)).save(userMock);

        assertNotNull(response);
    }

    @Test
    @DisplayName("Register user bad username")
    void registerUserBadUsername() {
        UserDto toSave = getUserDtoMock();
        String invalidSymbols = "!@#$%^&*()+-=[]{}'|.>,</?`~л ";

        String username = "username";
        for (Character symbol : invalidSymbols.toCharArray()) {
            toSave.setUsername(symbol + username);
            AppException exception = assertThrows(AppException.class, () -> authenticationService.register(toSave, Role.USER));
            assertTrue(exception.getMessage().contains("Username"));
        }
    }

    @Test
    @DisplayName("Register admin bad password")
    void registerAdminBadPassword() {
        UserDto toSave = getUserDtoMock();
        List<String> badPasswords = List.of("test1234", "mysuperpassword123!@#", "MySuperPassword!@#", "mysuperpassword",
                "Password1234", "No_Passworddddd", "!Password123л");

        for (String password : badPasswords) {
            toSave.setPassword(password);
            AppException exception = assertThrows(AppException.class, () -> authenticationService.register(toSave, Role.ADMIN));
            assertTrue(exception.getMessage().contains("Password") || exception.getMessage().contains("password"));
        }
    }

    // TODO refactor with email confirmation
    @Test
    @DisplayName("Authenticate user")
    void authenticate() {
        UserDto userDto = getUserDtoMock();
        User user = getUserMock();
        Token refreshToken = getRefreshTokenMock();

        AuthenticationRequest request = new AuthenticationRequest(userDto.getUsername(), userDto.getPassword());
        when(authenticationManager.authenticate(any())).thenReturn(any());

        when(userRepository.findByUsername(request.getUsername())).thenReturn(Optional.of(user));
        when(jwtService.generateToken(user)).thenReturn("token");
        when(tokenService.createToken(user, TokenType.REFRESH)).thenReturn(refreshToken);

        AuthenticationResponse response = authenticationService.authenticate(request);

        assertEquals("token", response.getToken());
        assertEquals(user.getUsername(), response.getUser().getUsername());
        assertEquals(refreshToken.getToken(), response.getRefreshToken());
    }

    @Test
    @DisplayName("Refresh token")
    void refreshToken() {
        Token refreshToken = getRefreshTokenMock();
        refreshToken.setExpiryDate(Instant.now().plusSeconds(60));

        when(tokenService.findByTokenAndType(refreshToken.getToken(), TokenType.REFRESH)).thenReturn(refreshToken);
        when(jwtService.generateToken(any())).thenReturn("token");

        TokenRefreshResponse response = authenticationService.refreshToken(new TokenRefreshRequest(refreshToken.getToken()));

        assertEquals("token", response.getToken());
    }

    @Test
    @DisplayName("Refresh token expired")
    void refreshExpiredToken() {
        Token refreshToken = getRefreshTokenMock();

        when(tokenService.findByTokenAndType(refreshToken.getToken(), TokenType.REFRESH)).thenReturn(refreshToken);
        when(tokenService.verifyExpiration(refreshToken)).thenThrow(
                new AppException("Token expired. Please make a new request", HttpStatus.UNAUTHORIZED));

        AppException exception = assertThrows(AppException.class,
                () -> authenticationService.refreshToken(new TokenRefreshRequest(refreshToken.getToken())));

        assertEquals("Token expired. Please make a new request", exception.getMessage());
        assertEquals(HttpStatus.UNAUTHORIZED, exception.getHttpStatus());
    }

    @Test
    @DisplayName("OAuth confirmation")
    void oAuthConfirmation() {
        applicationConfig.setProduction(true);

        Token confirmationToken = getConfirmationTokenMock();
        User user = confirmationToken.getUser();
        user.setActive(false);
        String username = user.getUsername();


        when(tokenService.findByTokenAndType(confirmationToken.getToken(), TokenType.CONFIRMATION)).thenReturn(confirmationToken);
        Token refreshToken = getRefreshTokenMock();
        when(tokenService.createToken(user, TokenType.REFRESH)).thenReturn(refreshToken);
        when(jwtService.generateToken(user)).thenReturn("token");
        ResponseCookie cookie = ResponseCookie.from("confirmationToken").value(null).build();
        when(cookieService.resetConfirmationCookie()).thenReturn(cookie);

        AuthenticationResponse res = authenticationService.oAuthConfirmation(username, confirmationToken.getToken());
        UserDto savedUser = res.getUser();

        verify(userMapper, times(1)).toDto(user);
        verify(userRepository, times(1)).save(user);
        verify(jwtService, times(1)).generateToken(user);
        verify(tokenService, times(1)).createToken(user, TokenType.REFRESH);
        verify(userProducer, times(1)).sendUserCreated(user.getId());

        assertEquals(savedUser.getName(), user.getName());
        assertTrue(user.getActive());
        assertFalse(user.getFrozen());
        assertEquals(savedUser.getEmail(), user.getEmail());
        assertEquals(savedUser.getUsername(), user.getUsername());
        assertNotNull(res.getRefreshToken());
        assertNotNull(res.getToken());
        assertTrue(res.getHeaders().containsKey(HttpHeaders.SET_COOKIE));
        assertTrue(res.getHeaders().containsValue(List.of(cookie.toString())));
    }

    @Test
    @DisplayName("OAuth confirmation token expired")
    void oAuthConfirmationTokenExpired() {
        Token confirmationToken = getConfirmationTokenMock();

        when(tokenService.findByTokenAndType(confirmationToken.getToken(), TokenType.CONFIRMATION)).thenReturn(confirmationToken);
        when(tokenService.verifyExpiration(confirmationToken)).thenThrow(
                new AppException("Token expired. Please make a new request", HttpStatus.UNAUTHORIZED));

        AppException exception = assertThrows(AppException.class,
                () -> authenticationService.oAuthConfirmation(confirmationToken.getUser().getUsername(), confirmationToken.getToken()));

        assertEquals("Token expired. Please make a new request", exception.getMessage());
        assertEquals(HttpStatus.UNAUTHORIZED, exception.getHttpStatus());
    }

    @Test
    @DisplayName("OAuth confirmation bad username")
    void oAuthConfirmationBadUsername() {
        Token confirmationToken = getConfirmationTokenMock();
        User user = confirmationToken.getUser();
        user.setActive(false);

        when(tokenService.findByTokenAndType(confirmationToken.getToken(), TokenType.CONFIRMATION)).thenReturn(confirmationToken);

        String invalidSymbols = "!@#$%^&*()+-=[]{}'|.>,</?`~л ";

        String username = "username";
        for (Character symbol : invalidSymbols.toCharArray()) {
            String invalidUsername = symbol + username;
            AppException exception = assertThrows(AppException.class,
                    () -> authenticationService.oAuthConfirmation(invalidUsername, confirmationToken.getToken()));
            assertTrue(exception.getMessage().contains("Username"));
        }
    }

    @Test
    @DisplayName("OAuth confirmation the user already active")
    void oAuthConfirmationUserAlreadyActive() {
        Token confirmationToken = getConfirmationTokenMock();
        User user = confirmationToken.getUser();
        user.setActive(true);

        when(tokenService.findByTokenAndType(confirmationToken.getToken(), TokenType.CONFIRMATION)).thenReturn(confirmationToken);

        assertThrows(AppException.class,
                () -> authenticationService.oAuthConfirmation(user.getUsername(), confirmationToken.getToken()));
    }
}
