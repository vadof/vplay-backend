package com.vcasino.user.mock;

import com.vcasino.user.dto.email.EmailTokenOptionsDto;
import com.vcasino.user.entity.Token;
import com.vcasino.user.entity.TokenType;

import java.time.Instant;

import static com.vcasino.user.mock.UserMocks.getUserMock;

public class TokenMocks {
    public static Token getRefreshTokenMock() {
        return createToken(TokenType.REFRESH, "refreshToken");
    }

    public static Token getUsernameConfirmationTokenMock() {
        return createToken(TokenType.USERNAME_CONFIRMATION, "usernameConfirmationToken");
    }

    public static Token getEmailConfirmationTokenMock() {
        Token token = createToken(TokenType.EMAIL_CONFIRMATION, "emailConfirmationToken");
        token.setOptions("{\"resendToken\":\"emailResendToken\",\"emailsSent\":1,\"sentAt\":\"2025-01-01T00:00:00.0Z\"}");
        return token;
    }

    public static Token.EmailTokenOptions getEmailConfirmationTokenOptions() {
        return new Token.EmailTokenOptions("emailResendToken", 1, Instant.now());
    }

    public static EmailTokenOptionsDto getEmailConfirmationTokenOptionsDto() {
        return new EmailTokenOptionsDto("test@gmail.com", "emailResendToken", 1, true);
    }

    private static Token createToken(TokenType type, String token) {
        return Token.builder()
                .id(1L)
                .user(getUserMock(type == TokenType.REFRESH))
                .token(token)
                .expiryDate(Instant.now().plusSeconds(60 * 60))
                .type(type)
                .build();
    }
}
