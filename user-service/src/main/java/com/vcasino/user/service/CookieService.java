package com.vcasino.user.service;

import com.vcasino.user.config.ApplicationConfig;
import com.vcasino.user.entity.Token;
import jakarta.servlet.http.Cookie;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;

@Service
@AllArgsConstructor
@Slf4j
public class CookieService {

    private final ApplicationConfig config;

    public Cookie generateCookie(String key, String value, int maxAgeSeconds, boolean httpOnly) {
        Cookie cookie = new Cookie(key, value);
        cookie.setPath("/");
        cookie.setHttpOnly(httpOnly);
        cookie.setMaxAge(maxAgeSeconds);
        cookie.setSecure(config.getProduction());
        return cookie;
    }

    public ResponseCookie generateResponseCookie(String key, String value, int maxAgeSeconds, boolean httpOnly) {
        return ResponseCookie.from(key, value)
                .path("/")
                .httpOnly(httpOnly)
                .maxAge(maxAgeSeconds)
                .secure(config.getProduction())
                .build();
    }

    public Cookie generateConfirmationCookie(Token token) {
        int maxAge = (int) Duration.between(Instant.now(), token.getExpiryDate()).getSeconds();
        return generateCookie("confirmationToken", token.getToken(), maxAge, false);
    }

    public ResponseCookie resetConfirmationCookie() {
        return generateResponseCookie("confirmationToken", "", 0, false);
    }

    public Cookie generateJwtCookie(String jwtToken) {
        return generateCookie("accessToken", jwtToken,
                (int) config.getJwt().getExpirationMs() / 1000, false);
    }

    public Cookie generateJwtRefreshCookie(Token refreshToken) {
        return generateCookie("refreshToken", refreshToken.getToken(),
                (int) config.getJwt().getRefreshExpirationMs() / 1000, false);
    }
}
