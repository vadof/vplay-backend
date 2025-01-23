package com.vcasino.user.oauth2;

import com.vcasino.user.config.ApplicationConfig;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Component
@Slf4j
public class OAuth2LoginFailureHandler implements AuthenticationFailureHandler {

    private final String REDIRECT_URL;

    public OAuth2LoginFailureHandler(ApplicationConfig config) {
        String errorMessage = "Authentication failed, please try again or choose another method";
        String encodedMessage = URLEncoder.encode(errorMessage, StandardCharsets.UTF_8);
        this.REDIRECT_URL = config.getClientUrl() + "/login?oauthError=" + encodedMessage;
    }

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException {
        log.error("OAuth2 Authentication failed: {}", exception.getMessage());
        response.sendRedirect(REDIRECT_URL);
    }
}
