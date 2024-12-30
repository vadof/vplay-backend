package com.vcasino.user.mock;

import com.vcasino.user.entity.Token;
import com.vcasino.user.entity.TokenType;

import java.time.Instant;

import static com.vcasino.user.mock.UserMocks.getUserMock;

public class RefreshTokenMocks {
    public static Token getRefreshTokenMock() {
        return new Token(1L, getUserMock(), "refreshToken", Instant.now(), TokenType.REFRESH);
    }

    public static Token getVerificationTokenMock() {
        return new Token(1L, getUserMock(), "refreshToken", Instant.now(), TokenType.VERIFICATION);
    }
}
