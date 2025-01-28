package com.vcasino.user.mock;

import com.vcasino.user.entity.Token;
import com.vcasino.user.entity.TokenType;

import java.time.Instant;

import static com.vcasino.user.mock.UserMocks.getUserMock;

public class TokenMocks {
    public static Token getRefreshTokenMock() {
        return Token.builder()
                .id(1L)
                .user(getUserMock())
                .token("refreshToken")
                .expiryDate(Instant.now().minusSeconds(60 * 60))
                .type(TokenType.REFRESH)
                .build();
    }

    public static Token getConfirmationTokenMock() {
        return Token.builder()
                .id(1L)
                .user(getUserMock())
                .token("confirmationToken")
                .expiryDate(Instant.now().minusSeconds(60 * 60))
                .type(TokenType.USERNAME_CONFIRMATION)
                .build();
    }
}
