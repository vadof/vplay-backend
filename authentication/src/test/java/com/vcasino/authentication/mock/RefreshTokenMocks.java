package com.vcasino.authentication.mock;

import com.vcasino.authentication.entity.RefreshToken;

import java.time.Instant;

import static com.vcasino.authentication.mock.UserMocks.getUserMock;

public class RefreshTokenMocks {
    public static RefreshToken getRefreshTokenMock() {
        return new RefreshToken(1L, getUserMock(), "refreshToken", Instant.now());
    }
}
