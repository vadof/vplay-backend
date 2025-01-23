package com.vcasino.user.service;

import com.vcasino.user.config.ApplicationConfig;
import com.vcasino.user.entity.Token;
import com.vcasino.user.entity.TokenType;
import com.vcasino.user.entity.User;
import com.vcasino.user.exception.AppException;
import com.vcasino.user.repository.TokenRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.lang.reflect.Field;
import java.time.Instant;
import java.util.Optional;

import static com.vcasino.user.mock.TokenMocks.getConfirmationTokenMock;
import static com.vcasino.user.mock.TokenMocks.getRefreshTokenMock;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * UNIT tests for {@link TokenService}
 */
@ExtendWith(MockitoExtension.class)
public class TokenServiceTests {

    @Mock
    private TokenRepository tokenRepository;

    @InjectMocks
    private TokenService tokenService;

    private final long TOKEN_EXPIRATION_MS = 1000 * 60 * 60;

    @BeforeEach
    void initConfig() throws Exception {
        ApplicationConfig config = new ApplicationConfig();
        Field field = TokenService.class.getDeclaredField("config");
        field.setAccessible(true);
        field.set(tokenService, config);

        var confirmation = new ApplicationConfig.ConfirmationProperties();
        confirmation.setToken(new ApplicationConfig.ConfirmationProperties.TokenProperties());
        confirmation.getToken().setExpirationMs(TOKEN_EXPIRATION_MS);

        var jwt = new ApplicationConfig.JwtProperties();
        jwt.setExpirationMs(TOKEN_EXPIRATION_MS);

        config.setConfirmation(confirmation);
        config.setJwt(jwt);
    }

    @Test
    @DisplayName("Find by token and type")
    void findByTokenAndType() {
        Token refreshToken = getRefreshTokenMock();
        Token confirmationToken = getConfirmationTokenMock();

        when(tokenRepository.findByTokenAndType(refreshToken.getToken(), TokenType.REFRESH))
                .thenReturn(Optional.of(refreshToken));

        Token res = tokenService.findByTokenAndType(refreshToken.getToken(), refreshToken.getType());
        assertEquals(refreshToken, res);

        when(tokenRepository.findByTokenAndType(confirmationToken.getToken(), TokenType.CONFIRMATION))
                .thenReturn(Optional.of(confirmationToken));

        res = tokenService.findByTokenAndType(confirmationToken.getToken(), confirmationToken.getType());
        assertEquals(confirmationToken, res);

        when(tokenRepository.findByTokenAndType(refreshToken.getToken() + "1", TokenType.REFRESH))
                .thenReturn(Optional.empty());

        assertThrows(AppException.class,
                () -> tokenService.findByTokenAndType(refreshToken.getToken() + "1", refreshToken.getType()));

        when(tokenRepository.findByTokenAndType(confirmationToken.getToken() + "1", TokenType.CONFIRMATION))
                .thenReturn(Optional.empty());

        assertThrows(AppException.class,
                () -> tokenService.findByTokenAndType(confirmationToken.getToken() + "1", confirmationToken.getType()));
    }

    @Test
    @DisplayName("Create token")
    void createToken() {
        Token token = getRefreshTokenMock();
        User tokenUser = token.getUser();
        String tokenValue = token.getToken();
        when(tokenRepository.findByUser(token.getUser())).thenReturn(Optional.of(token));

        tokenService.createToken(tokenUser, TokenType.REFRESH);

        verify(tokenRepository, times(1)).save(token);

        assertEquals(TokenType.REFRESH, token.getType());
        assertEquals(tokenUser, token.getUser());
        assertNotEquals(tokenValue, token.getToken());

        Instant less = Instant.now().minusMillis((long) (TOKEN_EXPIRATION_MS * 1.1));
        Instant more = Instant.now().plusMillis((long) (TOKEN_EXPIRATION_MS * 0.1));
        assertTrue(token.getExpiryDate().isAfter(less) && token.getExpiryDate().isBefore(more));
    }

    @Test
    @DisplayName("Create token different type")
    void createTokenDifferentType() {
        Token token = getRefreshTokenMock();
        String tokenValue = token.getToken();
        when(tokenRepository.findByUser(token.getUser())).thenReturn(Optional.of(token));

        tokenService.createToken(token.getUser(), TokenType.CONFIRMATION);

        verify(tokenRepository, times(1)).save(token);

        assertEquals(TokenType.CONFIRMATION, token.getType());
        assertNotEquals(tokenValue, token.getToken());
    }

    @Test
    @DisplayName("Create token user not found")
    void createTokenUserNotFound() {
        Token token = getRefreshTokenMock();
        User tokenUser = token.getUser();
        when(tokenRepository.findByUser(token.getUser())).thenReturn(Optional.empty());

        tokenService.createToken(tokenUser, TokenType.REFRESH);

        verify(tokenRepository, times(1)).save(any(Token.class));

        assertEquals(TokenType.REFRESH, token.getType());
        assertEquals(token.getUser(), tokenUser);
    }

    @Test
    @DisplayName("Verify expiration")
    void verifyExpiration() {
        Token token = getRefreshTokenMock();
        token.setExpiryDate(Instant.now().minusSeconds(60));

        AppException exception = assertThrows(AppException.class, () -> tokenService.verifyExpiration(token));

        verify(tokenRepository, times(1)).delete(token);
        assertTrue(exception.getMessage().contains("expired"));
        assertEquals(HttpStatus.FORBIDDEN, exception.getHttpStatus());

        token.setExpiryDate(Instant.now().plusSeconds(60));
        Token res = tokenService.verifyExpiration(token);
        assertEquals(res, token);
    }

}
