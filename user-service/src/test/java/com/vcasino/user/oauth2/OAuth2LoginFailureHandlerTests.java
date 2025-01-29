package com.vcasino.user.oauth2;

import com.vcasino.user.config.ApplicationConfig;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.AuthenticationException;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * UNIT tests for {@link OAuth2LoginFailureHandler}
 */
@ExtendWith(MockitoExtension.class)
public class OAuth2LoginFailureHandlerTests {

    private OAuth2LoginFailureHandler failureHandler;

    @Mock
    private ApplicationConfig config;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private AuthenticationException exception;

    @BeforeEach
    void setUp() {
        try (AutoCloseable ignored = MockitoAnnotations.openMocks(this)) {
            when(config.getClientUrl()).thenReturn("https://domain.com");
            failureHandler = new OAuth2LoginFailureHandler(config);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    @DisplayName("Authentication failure redirects correctly with error message")
    void authenticationFailureRedirectsCorrectlyWithMessage() throws IOException {
        String expectedErrorMessage = "Authentication failed, please try again or choose another method";
        String encodedMessage = URLEncoder.encode(expectedErrorMessage, StandardCharsets.UTF_8);
        String expectedRedirectUrl = "https://domain.com/login?oauthError=" + encodedMessage;

        failureHandler.onAuthenticationFailure(request, response, exception);
        verify(response).sendRedirect(expectedRedirectUrl);
    }
}

