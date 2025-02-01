package com.vcasino.user.service;

import com.vcasino.user.config.ApplicationConfig;
import com.vcasino.user.config.securiy.JwtService;
import com.vcasino.user.dto.UserDto;
import com.vcasino.user.dto.auth.AuthenticationRequest;
import com.vcasino.user.dto.auth.AuthenticationResponse;
import com.vcasino.user.dto.auth.TokenRefreshRequest;
import com.vcasino.user.dto.auth.TokenRefreshResponse;
import com.vcasino.user.dto.email.EmailTokenOptionsDto;
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

import static com.vcasino.user.mock.TokenMocks.getEmailConfirmationTokenMock;
import static com.vcasino.user.mock.TokenMocks.getEmailConfirmationTokenOptions;
import static com.vcasino.user.mock.TokenMocks.getEmailConfirmationTokenOptionsDto;
import static com.vcasino.user.mock.TokenMocks.getRefreshTokenMock;
import static com.vcasino.user.mock.TokenMocks.getUsernameConfirmationTokenMock;
import static com.vcasino.user.mock.UserMocks.getUserDtoMock;
import static com.vcasino.user.mock.UserMocks.getUserMock;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
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
    private EmailService emailService;

    @Mock
    private CookieService cookieService;

    private ApplicationConfig applicationConfig;

    @BeforeEach
    void initConfig() throws Exception {
        applicationConfig = new ApplicationConfig();
        Field field = AuthenticationService.class.getDeclaredField("applicationConfig");
        field.setAccessible(true);
        field.set(authenticationService, applicationConfig);
        applicationConfig.setClientUrl("https://domain.com");
    }

    @Test
    @DisplayName("Register user")
    void registerUser() {
        UserDto toSave = getUserDtoMock();

        mockFindByEmail(toSave, null);
        mockFindByUsername(toSave, null);

        when(userRepository.save(any(User.class))).thenReturn(getUserMock(false));
        mockEmailTokens();

        EmailTokenOptionsDto response = authenticationService.registerUser(toSave);

        verify(userRepository, times(1)).save(userArgumentCaptor.capture());

        User saved = userArgumentCaptor.getValue();

        checkSavedUser(toSave, saved, Role.USER, false, response);
    }

    @Test
    @DisplayName("Register admin")
    void registerAdmin() {
        applicationConfig.setProduction(true);
        String strongPassword = "!Password123";

        UserDto toSave = getUserDtoMock();
        toSave.setPassword(strongPassword);

        User adminMock = getUserMock(false);
        adminMock.setPassword(strongPassword);
        when(userRepository.save(any())).thenReturn(adminMock);

        authenticationService.registerAdmin(toSave);

        verify(userRepository, times(1)).save(userArgumentCaptor.capture());

        User saved = userArgumentCaptor.getValue();

        checkSavedUser(toSave, saved, Role.ADMIN, true, null);
    }

    @Test
    @DisplayName("Register user email already in use by active user")
    void registerEmailInUseByActiveUser() {
        UserDto toSave = getUserDtoMock();
        User existingUser = getUserMock(false);
        existingUser.setActive(true);
        mockFindByEmail(toSave, existingUser);

        AppException exception = assertThrows(AppException.class, () -> authenticationService.registerUser(toSave));

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
        User existingUser = getUserMock(false);
        existingUser.setRegisterDate(now.minusMinutes(1));

        mockFindByEmail(toSave, existingUser);

        AppException exception = assertThrows(AppException.class, () -> authenticationService.registerUser(toSave));

        assertTrue(exception.getMessage().contains("Email"));
        assertEquals(HttpStatus.BAD_REQUEST, exception.getHttpStatus());

        existingUser.setRegisterDate(now.minusMinutes(tokenExpirationInMinutes * 2));

        User userMock = getUserMock(false);
        when(userRepository.save(any())).thenReturn(userMock);
        mockEmailTokens();

        EmailTokenOptionsDto response = authenticationService.registerUser(toSave);
        verify(tokenService, times(1)).deleteTokenByUser(existingUser);
        verify(userRepository, times(1)).delete(existingUser);

        checkSavedUser(toSave, userMock, Role.USER, false, response);
    }

    @Test
    @DisplayName("Register user username already in use by active user")
    void registerUsernameInUseByActiveUser() {
        UserDto toSave = getUserDtoMock();
        User existingUser = getUserMock(true);
        mockFindByUsername(toSave, existingUser);

        AppException exception = assertThrows(AppException.class, () -> authenticationService.registerUser(toSave));

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
        User existingUser = getUserMock(false);
        existingUser.setRegisterDate(now.minusMinutes(1));

        mockFindByUsername(toSave, existingUser);

        AppException exception = assertThrows(AppException.class, () -> authenticationService.registerUser(toSave));

        assertTrue(exception.getMessage().contains("Username"));
        assertEquals(HttpStatus.BAD_REQUEST, exception.getHttpStatus());

        existingUser.setRegisterDate(now.minusMinutes(tokenExpirationInMinutes * 2));

        User userMock = getUserMock(false);
        when(userRepository.save(any())).thenReturn(userMock);
        mockEmailTokens();

        EmailTokenOptionsDto response = authenticationService.registerUser(toSave);
        verify(tokenService, times(1)).deleteTokenByUser(existingUser);
        verify(userRepository, times(1)).delete(existingUser);

        checkSavedUser(toSave, userMock, Role.USER, false, response);
    }


    @Test
    @DisplayName("Register user bad username")
    void registerUserBadUsername() {
        UserDto toSave = getUserDtoMock();
        String invalidSymbols = "!@#$%^&*()+-=[]{}'|.>,</?`~л ";

        String username = "username";
        for (Character symbol : invalidSymbols.toCharArray()) {
            toSave.setUsername(symbol + username);
            AppException exception = assertThrows(AppException.class, () -> authenticationService.registerUser(toSave));
            assertTrue(exception.getMessage().contains("Username"));
        }
    }

    @Test
    @DisplayName("Register user bad password")
    void registerUserBadPassword() {
        UserDto toSave = getUserDtoMock();
        String invalidSymbols = "лñ打";

        String password = "test1234";
        for (Character symbol : invalidSymbols.toCharArray()) {
            toSave.setPassword(password + symbol);
            AppException exception = assertThrows(AppException.class, () -> authenticationService.registerUser(toSave));
            assertTrue(exception.getMessage().contains("Password"));
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
            AppException exception = assertThrows(AppException.class, () -> authenticationService.registerAdmin(toSave));
            assertTrue(exception.getMessage().contains("Password") || exception.getMessage().contains("password"));
        }
    }

    @Test
    @DisplayName("Refresh token")
    void refreshToken() {
        Token refreshToken = getRefreshTokenMock();
        refreshToken.setExpiryDate(Instant.now().plusSeconds(60));

        when(tokenService.findByTokenAndType(refreshToken.getToken(), TokenType.REFRESH)).thenReturn(refreshToken);
        when(jwtService.generateJwtToken(any())).thenReturn("token");

        TokenRefreshResponse response = authenticationService.refreshToken(new TokenRefreshRequest(refreshToken.getToken()));

        assertEquals("token", response.getToken());
    }

    @Test
    @DisplayName("Refresh token expired")
    void refreshExpiredToken() {
        Token refreshToken = getRefreshTokenMock();

        when(tokenService.findByTokenAndType(refreshToken.getToken(), TokenType.REFRESH)).thenReturn(refreshToken);
        when(tokenService.verifyExpiration(refreshToken, null)).thenThrow(
                new AppException("Token expired. Please make a new request", HttpStatus.UNAUTHORIZED));

        AppException exception = assertThrows(AppException.class,
                () -> authenticationService.refreshToken(new TokenRefreshRequest(refreshToken.getToken())));

        assertEquals("Token expired. Please make a new request", exception.getMessage());
        assertEquals(HttpStatus.UNAUTHORIZED, exception.getHttpStatus());
    }

    @Test
    @DisplayName("Authenticate user")
    void authenticateUser() {
        UserDto userDto = getUserDtoMock();
        User user = getUserMock(true);
        Token refreshToken = getRefreshTokenMock();

        AuthenticationRequest request = new AuthenticationRequest(userDto.getUsername(), userDto.getPassword());
        when(authenticationManager.authenticate(any())).thenReturn(any());

        when(userRepository.findByUsername(request.getUsername())).thenReturn(Optional.of(user));
        when(jwtService.generateJwtToken(user)).thenReturn("token");
        when(tokenService.createToken(user, TokenType.REFRESH)).thenReturn(refreshToken);

        AuthenticationResponse response = authenticationService.authenticate(request);

        assertEquals("token", response.getToken());
        assertEquals(user.getUsername(), response.getUser().getUsername());
        assertEquals(refreshToken.getToken(), response.getRefreshToken());
    }

    @Test
    @DisplayName("Authenticate inactive user")
    void authenticateInactiveUser() {
        UserDto userDto = getUserDtoMock();
        User user = getUserMock(false);
        Token confirmationToken = getEmailConfirmationTokenMock();

        AuthenticationRequest request = new AuthenticationRequest(userDto.getUsername(), userDto.getPassword());
        when(authenticationManager.authenticate(any())).thenReturn(any());

        when(userRepository.findByUsername(request.getUsername())).thenReturn(Optional.of(user));
        when(tokenService.findByUserAndType(user, TokenType.EMAIL_CONFIRMATION)).thenReturn(Optional.of(confirmationToken));
        when(tokenService.isTokenExpired(confirmationToken)).thenReturn(false);

        AppException exception = assertThrows(AppException.class, () -> authenticationService.authenticate(request));

        assertTrue(exception.getMessage().contains("Check"));
        assertTrue(exception.getMessage().contains("email"));
    }

    @Test
    @DisplayName("Authenticate inactive user confirmation token not found")
    void authenticateInactiveUserConfirmationTokenNotFound() {
        UserDto userDto = getUserDtoMock();
        User user = getUserMock(false);

        AuthenticationRequest request = new AuthenticationRequest(userDto.getUsername(), userDto.getPassword());
        when(authenticationManager.authenticate(any())).thenReturn(any());

        when(userRepository.findByUsername(request.getUsername())).thenReturn(Optional.of(user));
        when(tokenService.findByUserAndType(user, TokenType.EMAIL_CONFIRMATION)).thenReturn(Optional.empty());

        assertThrows(AppException.class, () -> authenticationService.authenticate(request));

        verify(userRepository, times(1)).delete(user);
    }

    @Test
    @DisplayName("Authenticate inactive user with expired confirmation token")
    void authenticateInactiveUserExpiredConfirmationToken() {
        UserDto userDto = getUserDtoMock();
        User user = getUserMock(false);
        Token confirmationToken = getEmailConfirmationTokenMock();

        AuthenticationRequest request = new AuthenticationRequest(userDto.getUsername(), userDto.getPassword());
        when(authenticationManager.authenticate(any())).thenReturn(any());

        when(userRepository.findByUsername(request.getUsername())).thenReturn(Optional.of(user));
        when(tokenService.findByUserAndType(user, TokenType.EMAIL_CONFIRMATION)).thenReturn(Optional.of(confirmationToken));
        when(tokenService.isTokenExpired(confirmationToken)).thenReturn(true);

        AppException exception = assertThrows(AppException.class, () -> authenticationService.authenticate(request));

        assertEquals("Invalid Username or Password!", exception.getMessage());
        verify(tokenService, times(1)).deleteTokenByUser(user);
        verify(userRepository, times(1)).delete(user);
    }

    @Test
    @DisplayName("Username confirmation")
    void usernameConfirmation() {
        Token confirmationToken = getUsernameConfirmationTokenMock();
        when(tokenService.findByTokenAndType(confirmationToken.getToken(), TokenType.USERNAME_CONFIRMATION)).thenReturn(confirmationToken);
        when(tokenService.isTokenExpired(confirmationToken)).thenReturn(false);

        User user = confirmationToken.getUser();
        String username = user.getUsername();
        Token refreshToken = getRefreshTokenMock();
        when(jwtService.generateJwtToken(user)).thenReturn("token");
        when(tokenService.createToken(user, TokenType.REFRESH)).thenReturn(refreshToken);

        ResponseCookie cookie = ResponseCookie.from("usernameConfirmationToken").value(null).build();
        when(cookieService.resetConfirmationCookie()).thenReturn(cookie);

        AuthenticationResponse res = authenticationService.confirmUsername(username, confirmationToken.getToken());
        UserDto savedUser = res.getUser();

        verify(userMapper, times(1)).toDto(user);
        verify(userRepository, times(1)).save(user);
        verify(jwtService, times(1)).generateJwtToken(user);
        verify(tokenService, times(1)).createToken(user, TokenType.REFRESH);
        verify(userProducer, times(1)).sendUserCreated(user.getId());

        assertEquals(savedUser.getName(), user.getName());
        assertTrue(user.getActive());
        assertFalse(user.getFrozen());
        assertNotNull(user.getModifiedAt());
        assertEquals(savedUser.getEmail(), user.getEmail());
        assertEquals(savedUser.getUsername(), user.getUsername());
        assertNotNull(res.getRefreshToken());
        assertNotNull(res.getToken());
        assertTrue(res.getHeaders().containsKey(HttpHeaders.SET_COOKIE));
        assertTrue(res.getHeaders().containsValue(List.of(cookie.toString())));
    }

    @Test
    @DisplayName("Username confirmation another username")
    void usernameConfirmationAnotherUsername() {
        Token confirmationToken = getUsernameConfirmationTokenMock();
        when(tokenService.findByTokenAndType(confirmationToken.getToken(), TokenType.USERNAME_CONFIRMATION)).thenReturn(confirmationToken);
        when(tokenService.isTokenExpired(confirmationToken)).thenReturn(false);

        User user = confirmationToken.getUser();
        String newUsername = confirmationToken.getUser().getUsername() + "1";

        Token refreshToken = getRefreshTokenMock();

        when(userRepository.findByUsername(newUsername)).thenReturn(Optional.empty());

        when(jwtService.generateJwtToken(user)).thenReturn("token");
        when(tokenService.createToken(user, TokenType.REFRESH)).thenReturn(refreshToken);

        ResponseCookie cookie = ResponseCookie.from("usernameConfirmationToken").value(null).build();
        when(cookieService.resetConfirmationCookie()).thenReturn(cookie);

        AuthenticationResponse res = authenticationService.confirmUsername(newUsername, confirmationToken.getToken());
        UserDto savedUser = res.getUser();

        verify(userRepository, times(1)).save(user);
        assertEquals(newUsername, savedUser.getUsername());
    }

    @Test
    @DisplayName("Username confirmation another username already taken")
    void usernameConfirmationAnotherUsernameAlreadyTaken() {
        Token confirmationToken = getUsernameConfirmationTokenMock();
        when(tokenService.findByTokenAndType(confirmationToken.getToken(), TokenType.USERNAME_CONFIRMATION)).thenReturn(confirmationToken);
        when(tokenService.isTokenExpired(confirmationToken)).thenReturn(false);

        String newUsername = confirmationToken.getUser().getUsername() + "1";

        User existingUser = getUserMock(true);
        when(userRepository.findByUsername(newUsername)).thenReturn(Optional.of(existingUser));

        AppException exception = assertThrows(AppException.class,
                () -> authenticationService.confirmUsername(newUsername, confirmationToken.getToken()));

        assertTrue(exception.getMessage().contains("Username"));
    }

    @Test
    @DisplayName("Username confirmation bad username")
    void usernameConfirmationBadUsername() {
        Token confirmationToken = getUsernameConfirmationTokenMock();

        when(tokenService.findByTokenAndType(confirmationToken.getToken(), TokenType.USERNAME_CONFIRMATION)).thenReturn(confirmationToken);

        String invalidSymbols = "!@#$%^&*()+-=[]{}'|.>,</?`~л ";

        String username = "username";
        for (Character symbol : invalidSymbols.toCharArray()) {
            String invalidUsername = symbol + username;
            AppException exception = assertThrows(AppException.class,
                    () -> authenticationService.confirmUsername(invalidUsername, confirmationToken.getToken()));
            assertTrue(exception.getMessage().contains("Username"));
        }
    }

    @Test
    @DisplayName("Username confirmation token expired")
    void usernameConfirmationTokenExpired() {
        confirmationTokenExpired(getUsernameConfirmationTokenMock());
    }

    @Test
    @DisplayName("Username confirmation the user already active")
    void usernameConfirmationUserAlreadyActive() {
        confirmationUserAlreadyActive(getUsernameConfirmationTokenMock());
    }

    @Test
    @DisplayName("Email confirmation")
    void emailConfirmation() {
        Token confirmationToken = getEmailConfirmationTokenMock();
        when(tokenService.findByTokenAndType(confirmationToken.getToken(), TokenType.EMAIL_CONFIRMATION)).thenReturn(confirmationToken);
        when(tokenService.isTokenExpired(confirmationToken)).thenReturn(false);

        User user = confirmationToken.getUser();
        Token refreshToken = getRefreshTokenMock();
        when(jwtService.generateJwtToken(user)).thenReturn("token");
        when(tokenService.createToken(user, TokenType.REFRESH)).thenReturn(refreshToken);

        AuthenticationResponse res = authenticationService.confirmEmail(confirmationToken.getToken());
        UserDto savedUser = res.getUser();

        verify(userMapper, times(1)).toDto(user);
        verify(userRepository, times(1)).save(user);
        verify(jwtService, times(1)).generateJwtToken(user);
        verify(tokenService, times(1)).createToken(user, TokenType.REFRESH);
        verify(userProducer, times(1)).sendUserCreated(user.getId());

        assertEquals(savedUser.getName(), user.getName());
        assertTrue(user.getActive());
        assertFalse(user.getFrozen());
        assertNotNull(user.getModifiedAt());
        assertEquals(savedUser.getEmail(), user.getEmail());
        assertEquals(savedUser.getUsername(), user.getUsername());
        assertNotNull(res.getRefreshToken());
        assertNotNull(res.getToken());
    }

    @Test
    @DisplayName("Email confirmation token expired")
    void emailConfirmationTokenExpired() {
        confirmationTokenExpired(getEmailConfirmationTokenMock());
    }

    @Test
    @DisplayName("Email confirmation the user already active")
    void emailConfirmationUserAlreadyActive() {
        confirmationUserAlreadyActive(getEmailConfirmationTokenMock());
    }

    @Test
    @DisplayName("Resend confirmation email")
    void resendConfirmationEmail() {
        User user = getUserMock(false);
        Token token = getEmailConfirmationTokenMock();
        Token.EmailTokenOptions options = getEmailConfirmationTokenOptions();
        options.setSentAt(Instant.now().minusSeconds(60));

        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(tokenService.findByUserAndType(user, token.getType())).thenReturn(Optional.of(token));
        when(tokenService.isTokenExpired(token)).thenReturn(false);
        when(tokenService.getEmailTokenOptionsFromToken(token)).thenReturn(options);

        EmailTokenOptionsDto optionsDto = getEmailConfirmationTokenOptionsDto();
        EmailTokenOptionsDto res = authenticationService.resendConfirmationEmail(optionsDto);

        verify(tokenService, times(1)).updateTokenOptions(token, options);
        verify(emailService, times(1)).send(eq(user.getEmail()), anyString());

        assertEquals(user.getEmail(), res.getEmail());
        assertEquals(options.getResendToken(), res.getResendToken());
        assertEquals(2, res.getEmailsSent());
        assertTrue(res.getCanResend());
    }

    @Test
    @DisplayName("Resend confirmation email limit reached")
    void resendConfirmationEmailLimitReached() {
        User user = getUserMock(false);
        Token token = getEmailConfirmationTokenMock();
        Token.EmailTokenOptions options = getEmailConfirmationTokenOptions();
        EmailTokenOptionsDto optionsDto = getEmailConfirmationTokenOptionsDto();

        for (int i = 2; i < 4; i++) {
            options.setSentAt(Instant.now().minusSeconds(300));

            when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
            when(tokenService.findByUserAndType(user, token.getType())).thenReturn(Optional.of(token));
            when(tokenService.isTokenExpired(token)).thenReturn(false);
            when(tokenService.getEmailTokenOptionsFromToken(token)).thenReturn(options);

            EmailTokenOptionsDto res = authenticationService.resendConfirmationEmail(optionsDto);

            assertEquals(user.getEmail(), res.getEmail());
            assertEquals(options.getResendToken(), res.getResendToken());
            assertEquals(i, res.getEmailsSent());
            assertEquals(i != 3, res.getCanResend());
        }

        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(tokenService.findByUserAndType(user, token.getType())).thenReturn(Optional.of(token));
        when(tokenService.isTokenExpired(token)).thenReturn(false);
        when(tokenService.getEmailTokenOptionsFromToken(token)).thenReturn(options);

        AppException exception = assertThrows(AppException.class,
                () -> authenticationService.resendConfirmationEmail(optionsDto));

        assertTrue(exception.getMessage().contains("limit"));
    }

    @Test
    @DisplayName("Resend confirmation email user or token not found")
    void resendConfirmationEmailUserOrTokenNotFound() {
        User user = getUserMock(false);
        EmailTokenOptionsDto optionsDto = getEmailConfirmationTokenOptionsDto();

        when(userRepository.findByEmail(optionsDto.getEmail())).thenReturn(Optional.empty());
        assertThrows(AppException.class, () -> authenticationService.resendConfirmationEmail(optionsDto));

        when(userRepository.findByEmail(optionsDto.getEmail())).thenReturn(Optional.of(user));
        when(tokenService.findByUserAndType(user, TokenType.EMAIL_CONFIRMATION)).thenReturn(Optional.empty());
        assertThrows(AppException.class, () -> authenticationService.resendConfirmationEmail(optionsDto));
    }

    @Test
    @DisplayName("Resend confirmation email token expired")
    void resendConfirmationEmailTokenExpired() {
        User user = getUserMock(false);
        Token token = getEmailConfirmationTokenMock();
        EmailTokenOptionsDto optionsDto = getEmailConfirmationTokenOptionsDto();

        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(tokenService.findByUserAndType(user, token.getType())).thenReturn(Optional.of(token));
        when(tokenService.isTokenExpired(token)).thenReturn(true);

        AppException exception = assertThrows(AppException.class,
                () -> authenticationService.resendConfirmationEmail(optionsDto));

        assertTrue(exception.getMessage().contains("expired"));

        verify(userRepository, times(1)).delete(user);
        verify(tokenService, times(1)).deleteTokenByUser(user);
    }

    @Test
    @DisplayName("Resend confirmation email wrong token")
    void resendConfirmationEmailWrongToken() {
        User user = getUserMock(false);
        Token token = getEmailConfirmationTokenMock();
        Token.EmailTokenOptions options = getEmailConfirmationTokenOptions();
        EmailTokenOptionsDto optionsDto = getEmailConfirmationTokenOptionsDto();
        optionsDto.setResendToken(options.getResendToken() + "1");

        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(tokenService.findByUserAndType(user, token.getType())).thenReturn(Optional.of(token));
        when(tokenService.isTokenExpired(token)).thenReturn(false);
        when(tokenService.getEmailTokenOptionsFromToken(token)).thenReturn(options);

        assertThrows(AppException.class, () -> authenticationService.resendConfirmationEmail(optionsDto));
    }

    @Test
    @DisplayName("Resend confirmation email not enough time has passed")
    void resendConfirmationEmailNotEnoughTimeHasPassed() {
        User user = getUserMock(false);
        Token token = getEmailConfirmationTokenMock();
        Token.EmailTokenOptions options = getEmailConfirmationTokenOptions();
        EmailTokenOptionsDto optionsDto = getEmailConfirmationTokenOptionsDto();
        options.setSentAt(Instant.now().minusSeconds(10));

        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(tokenService.findByUserAndType(user, token.getType())).thenReturn(Optional.of(token));
        when(tokenService.isTokenExpired(token)).thenReturn(false);
        when(tokenService.getEmailTokenOptionsFromToken(token)).thenReturn(options);

        AppException exception = assertThrows(AppException.class,
                () -> authenticationService.resendConfirmationEmail(optionsDto));

        assertEquals("Please wait before resending the email.", exception.getMessage());
    }

    @Test
    @DisplayName("Delete pending user")
    void deletePendingUser() {
        User user = getUserMock(false);
        Token token = getEmailConfirmationTokenMock();
        Token.EmailTokenOptions options = getEmailConfirmationTokenOptions();
        EmailTokenOptionsDto optionsDto = getEmailConfirmationTokenOptionsDto();

        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(tokenService.findByUserAndType(user, token.getType())).thenReturn(Optional.of(token));
        when(tokenService.getEmailTokenOptionsFromToken(token)).thenReturn(options);

        authenticationService.deletePendingUser(optionsDto);

        verify(userRepository, times(1)).delete(user);
        verify(tokenService, times(1)).deleteTokenByUser(user);
    }

    @Test
    @DisplayName("Delete pending user, user or token not found")
    void deletePendingUserUserOrTokenNotFound() {
        User user = getUserMock(false);
        Token token = getEmailConfirmationTokenMock();
        EmailTokenOptionsDto optionsDto = getEmailConfirmationTokenOptionsDto();

        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.empty());
        assertThrows(AppException.class, () -> authenticationService.deletePendingUser(optionsDto));

        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(tokenService.findByUserAndType(user, token.getType())).thenReturn(Optional.empty());
        assertThrows(AppException.class, () -> authenticationService.deletePendingUser(optionsDto));
    }

    @Test
    @DisplayName("Delete pending user wrong token")
    void deletePendingUserUserWrongToken() {
        User user = getUserMock(false);
        Token token = getEmailConfirmationTokenMock();
        Token.EmailTokenOptions options = getEmailConfirmationTokenOptions();
        EmailTokenOptionsDto optionsDto = getEmailConfirmationTokenOptionsDto();
        optionsDto.setResendToken(options.getResendToken() + "1");

        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(tokenService.findByUserAndType(user, token.getType())).thenReturn(Optional.of(token));
        when(tokenService.getEmailTokenOptionsFromToken(token)).thenReturn(options);

        assertThrows(AppException.class, () -> authenticationService.deletePendingUser(optionsDto));
    }

    private void confirmationTokenExpired(Token confirmationToken) {
        when(tokenService.findByTokenAndType(confirmationToken.getToken(), confirmationToken.getType())).thenReturn(confirmationToken);
        when(tokenService.isTokenExpired(confirmationToken)).thenReturn(true);

        AppException exception = null;
        if (confirmationToken.getType().equals(TokenType.USERNAME_CONFIRMATION)) {
            exception = assertThrows(AppException.class,
                    () -> authenticationService.confirmUsername(
                            confirmationToken.getUser().getUsername(), confirmationToken.getToken())
            );
        } else if (confirmationToken.getType().equals(TokenType.EMAIL_CONFIRMATION)) {
            exception = assertThrows(AppException.class,
                    () -> authenticationService.confirmEmail(confirmationToken.getToken())
            );
        }

        assertNotNull(exception);
        assertEquals("Registration time has expired, please complete it again", exception.getMessage());
        assertEquals(HttpStatus.FORBIDDEN, exception.getHttpStatus());

        verify(userRepository, times(1)).delete(confirmationToken.getUser());
        verify(tokenService, times(1)).deleteTokenByUser(confirmationToken.getUser());
    }

    private void confirmationUserAlreadyActive(Token confirmationToken) {
        User user = confirmationToken.getUser();
        user.setActive(true);

        when(tokenService.findByTokenAndType(confirmationToken.getToken(), confirmationToken.getType())).thenReturn(confirmationToken);

        if (confirmationToken.getType().equals(TokenType.USERNAME_CONFIRMATION)) {
            assertThrows(AppException.class,
                    () -> authenticationService.confirmUsername(user.getUsername(), confirmationToken.getToken()));
        } else if (confirmationToken.getType().equals(TokenType.EMAIL_CONFIRMATION)) {
            assertThrows(AppException.class, () -> authenticationService.confirmEmail(confirmationToken.getToken()));
        }
    }

    private void mockFindByEmail(UserDto toSave, User found) {
        if (found != null) {
            when(userRepository.findByEmail(toSave.getEmail())).thenReturn(Optional.of(found));
        } else {
            when(userRepository.findByEmail(toSave.getEmail())).thenReturn(Optional.empty());
        }
    }

    private void mockFindByUsername(UserDto toSave, User found) {
        if (found != null) {
            when(userRepository.findByUsername(toSave.getUsername())).thenReturn(Optional.of(found));
        } else {
            when(userRepository.findByUsername(toSave.getUsername())).thenReturn(Optional.empty());
        }
    }

    private void mockEmailTokens() {
        Token emailConfirmationToken = getEmailConfirmationTokenMock();
        when(tokenService.createEmailConfirmationToken(any())).thenReturn(emailConfirmationToken);
        EmailTokenOptionsDto emailTokenOptions = getEmailConfirmationTokenOptionsDto();
        when(tokenService.getEmailTokenOptionsDtoFromToken(emailConfirmationToken)).thenReturn(emailTokenOptions);
    }

    private void checkSavedUser(UserDto toSave, User saved, Role role, boolean active, EmailTokenOptionsDto response) {
        verify(userMapper, times(1)).toEntity(toSave);
        verify(userRepository, times(1)).save(saved);
        assertNotNull(saved.getRegisterDate());
        assertEquals(role, saved.getRole());
        assertEquals(active, saved.getActive());
        assertFalse(saved.getFrozen());
        assertEquals(toSave.getName(), saved.getName());
        assertEquals(toSave.getEmail(), saved.getEmail());
        assertEquals(toSave.getUsername(), saved.getUsername());

        if (response != null) {
            verify(emailService, times(1)).send(eq(saved.getEmail()), anyString());
            checkEmailOptionsResponse(response, toSave.getEmail());
        }
    }

    private void checkEmailOptionsResponse(EmailTokenOptionsDto response, String email) {
        assertNotNull(response.getResendToken());
        assertEquals(response.getEmailsSent(), 1);
        assertEquals(response.getEmail(), email);
        assertTrue(response.getCanResend());
    }
}
