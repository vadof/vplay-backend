package com.vcasino.user.mock;

import com.vcasino.user.entity.RefreshToken;

import java.time.Instant;

import static com.vcasino.user.mock.UserMocks.getUserMock;

public class RefreshTokenMocks {
    public static RefreshToken getRefreshTokenMock() {
        return new RefreshToken(1L, getUserMock(), "refreshToken", Instant.now());
    }
}
