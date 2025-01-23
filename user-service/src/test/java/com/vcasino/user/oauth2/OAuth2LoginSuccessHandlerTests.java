package com.vcasino.user.oauth2;

import com.vcasino.user.config.ApplicationConfig;
import com.vcasino.user.config.securiy.JwtService;
import com.vcasino.user.entity.OAuthProvider;
import com.vcasino.user.entity.Role;
import com.vcasino.user.entity.Token;
import com.vcasino.user.entity.TokenType;
import com.vcasino.user.entity.User;
import com.vcasino.user.repository.TokenRepository;
import com.vcasino.user.repository.UserRepository;
import com.vcasino.user.service.CookieService;
import com.vcasino.user.service.TokenService;
import jakarta.servlet.http.Cookie;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.vcasino.user.mock.TokenMocks.getConfirmationTokenMock;
import static com.vcasino.user.mock.TokenMocks.getRefreshTokenMock;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * UNIT tests for {@link OAuth2LoginSuccessHandler}
 */
@ExtendWith(MockitoExtension.class)
public class OAuth2LoginSuccessHandlerTests {

    @Mock
    private UserRepository userRepository;
    @Mock
    private JwtService jwtService;
    @Mock
    private TokenService tokenService;
    @Mock
    private CookieService cookieService;
    @Mock
    private TokenRepository tokenRepository;
    @Captor
    private ArgumentCaptor<User> userArgumentCaptor;

    @InjectMocks
    private OAuth2LoginSuccessHandler oAuthHandler;

    private ApplicationConfig config;

    private final String ID = "11111111111";
    private final String EMAIL = "johndoe@gmail.com";
    private final String NAME = "John Doe";
    private final String USERNAME = "johndoe";

    @BeforeEach
    void initConfig() throws Exception {
        config = new ApplicationConfig();
        config.setClientUrl("https://domain.com");

        Field field = OAuth2LoginSuccessHandler.class.getDeclaredField("config");
        field.setAccessible(true);
        field.set(oAuthHandler, config);
    }

    private User getUserMock(OAuthProvider provider, boolean active) {
        return User.builder()
                .id(1L)
                .name(NAME)
                .username(USERNAME)
                .email(EMAIL)
                .role(Role.USER)
                .oauthProvider(provider)
                .oauthProviderId(ID)
                .active(active)
                .frozen(false)
                .build();
    }

    @Test
    @DisplayName("Authenticate active Google user")
    void authenticateGoogleUser() throws Exception {
        authenticateUserByProvider(OAuthProvider.GOOGLE);
    }

    @Test
    @DisplayName("Authenticate active Facebook user")
    void authenticateFacebookUser() throws Exception {
        authenticateUserByProvider(OAuthProvider.FACEBOOK);
    }

    @Test
    @DisplayName("Authenticate active Discord user")
    void authenticateDiscordUser() throws Exception {
        authenticateUserByProvider(OAuthProvider.DISCORD);
    }

    @Test
    @DisplayName("Authenticate active Github user")
    void authenticateGithubUser() throws Exception {
        authenticateUserByProvider(OAuthProvider.GITHUB);
    }

    private void authenticateUserByProvider(OAuthProvider provider) throws Exception {
        OAuth2AuthenticationToken oAuthToken = mockAuthentication(provider.toString().toLowerCase(), ID).getFirst();

        User user = getUserMock(provider, true);

        when(userRepository.findByOauthProviderAndOauthProviderId(provider, ID)).thenReturn(Optional.of(user));

        Entry<List<Cookie>, String> cookiesAndUrl = mockAuthenticationCookies(user);

        MockHttpServletResponse response = new MockHttpServletResponse();
        oAuthHandler.onAuthenticationSuccess(new MockHttpServletRequest(), response, oAuthToken);

        checkCookies(cookiesAndUrl.getFirst(), response);

        assertEquals(cookiesAndUrl.getSecond(), response.getRedirectedUrl());
    }

    private Entry<List<Cookie>, String> mockAuthenticationCookies(User user) {
        String jwtToken = "AAA-BBB-CCC";
        Token refreshToken = getRefreshTokenMock();

        when(jwtService.generateToken(user)).thenReturn(jwtToken);
        when(tokenService.createToken(user, TokenType.REFRESH)).thenReturn(refreshToken);

        Cookie jwtCookie = new Cookie("jwt", "AAA-BBB-CCC");
        Cookie refreshCookie = new Cookie("refresh", refreshToken.getToken());

        when(cookieService.generateJwtCookie("AAA-BBB-CCC")).thenReturn(jwtCookie);
        when(cookieService.generateJwtRefreshCookie(refreshToken)).thenReturn(refreshCookie);

        String expectedUrl = "%s/login/success?name=%s&username=%s&email=%s"
                .formatted(config.getClientUrl(), user.getName(), user.getUsername(), user.getEmail());

        return new Entry<>(List.of(jwtCookie, refreshCookie), expectedUrl);
    }

    @Test
    @DisplayName("Redirect inactive user to confirmation")
    void redirectInactiveUserToConfirmation() throws Exception {
        OAuthProvider provider = OAuthProvider.GOOGLE;
        OAuth2AuthenticationToken oAuthToken = mockAuthentication(provider.toString(), ID).getFirst();

        User user = getUserMock(provider, false);

        when(userRepository.findByOauthProviderAndOauthProviderId(provider, ID)).thenReturn(Optional.of(user));

        Token confirmationToken = getConfirmationTokenMock();
        when(tokenService.createToken(user, TokenType.CONFIRMATION)).thenReturn(confirmationToken);
        Cookie cookie = new Cookie("confirmationToken", confirmationToken.getToken());
        when(cookieService.generateConfirmationCookie(confirmationToken)).thenReturn(cookie);

        MockHttpServletResponse response = new MockHttpServletResponse();
        oAuthHandler.onAuthenticationSuccess(new MockHttpServletRequest(), response, oAuthToken);

        String expectedUrl = getConfirmationUrl(user.getUsername());
        assertEquals(expectedUrl, response.getRedirectedUrl());
        checkCookies(List.of(cookie), response);
    }

    @Test
    @DisplayName("Create pending Google user")
    void createPendingGoogleUser() throws Exception {
        OAuth2AuthenticationToken oAuthToken = mockGoogleAuthentication().getFirst();
        createPendingUser(oAuthToken, NAME, EMAIL, USERNAME, OAuthProvider.GOOGLE);
    }

    @Test
    @DisplayName("Create pending Facebook user")
    void createPendingFacebookUser() throws Exception {
        OAuth2AuthenticationToken oAuthToken = mockFacebookAuthentication().getFirst();
        String possibleUsername = NAME.replaceAll(" ", "_");
        createPendingUser(oAuthToken, NAME, null, possibleUsername, OAuthProvider.FACEBOOK);
    }

    @Test
    @DisplayName("Create pending Discord user")
    void createPendingDiscordUser() throws Exception {
        OAuth2AuthenticationToken oAuthToken = mockDiscordAuthentication().getFirst();
        createPendingUser(oAuthToken, null, EMAIL, USERNAME, OAuthProvider.DISCORD);
    }

    @Test
    @DisplayName("Create pending Github user")
    void createPendingGithubUser() throws Exception {
        OAuth2AuthenticationToken oAuthToken = mockGithubAuthentication().getFirst();
        createPendingUser(oAuthToken, NAME, null, USERNAME, OAuthProvider.GITHUB);
    }

    @Test
    @DisplayName("Create user, email already in use")
    void createUserEmailAlreadyInUse() throws Exception {
        OAuth2AuthenticationToken oAuthToken = mockDiscordAuthentication().getFirst();
        OAuthProvider provider = OAuthProvider.DISCORD;

        User existingUserByEmail = getUserMock(provider, true);
        existingUserByEmail.setOauthProvider(null);
        existingUserByEmail.setOauthProviderId(null);
        existingUserByEmail.setModifiedAt(null);

        when(userRepository.findByOauthProviderAndOauthProviderId(provider, ID)).thenReturn(Optional.empty());
        when(userRepository.findByUsernameOrEmail(USERNAME, EMAIL)).thenReturn(List.of(existingUserByEmail));

        Entry<List<Cookie>, String> cookiesAndUrl = mockAuthenticationCookies(existingUserByEmail);

        MockHttpServletResponse response = new MockHttpServletResponse();
        oAuthHandler.onAuthenticationSuccess(new MockHttpServletRequest(), response, oAuthToken);

        verify(userRepository, times(1)).save(existingUserByEmail);
        assertEquals(ID, existingUserByEmail.getOauthProviderId());
        assertEquals(provider, existingUserByEmail.getOauthProvider());
        assertNotNull(existingUserByEmail.getModifiedAt());
        checkCookies(cookiesAndUrl.getFirst(), response);
        assertEquals(cookiesAndUrl.getSecond(), response.getRedirectedUrl());
    }

    @Test
    @DisplayName("Create pending user, username already in use")
    void createPendingUserUsernameAlreadyInUse() {

    }

    @Test
    @DisplayName("Create pending user, email already in use but inactive user")
    void createPendingUserEmailAlreadyInUseInactiveUser() throws Exception {
        OAuth2AuthenticationToken oAuthToken = mockDiscordAuthentication().getFirst();
        OAuthProvider provider = OAuthProvider.DISCORD;

        User existingUserByEmail = getUserMock(provider, false);

        when(userRepository.findByOauthProviderAndOauthProviderId(provider, ID)).thenReturn(Optional.empty());
        when(userRepository.findByUsernameOrEmail(USERNAME, EMAIL)).thenReturn(List.of(existingUserByEmail));

        Token token = getConfirmationTokenMock();
        when(tokenService.createToken(any(), any())).thenReturn(token);
        Cookie cookie = new Cookie("confirmationToken", token.getToken());
        when(cookieService.generateConfirmationCookie(token)).thenReturn(cookie);

        MockHttpServletResponse response = new MockHttpServletResponse();
        oAuthHandler.onAuthenticationSuccess(new MockHttpServletRequest(), response, oAuthToken);

        verify(tokenRepository, times(1)).deleteByUser(existingUserByEmail);
        verify(userRepository, times(1)).delete(existingUserByEmail);
        verify(userRepository, times(1)).save(userArgumentCaptor.capture());

        User savedPendingUser = userArgumentCaptor.getValue();
        checkCreatedUser(savedPendingUser, null, EMAIL, USERNAME, provider, response, getConfirmationUrl(USERNAME), List.of(cookie));
    }

    void createPendingUser(OAuth2AuthenticationToken oAuthToken, String name, String email, String username, OAuthProvider provider) throws Exception {
        Token token = getConfirmationTokenMock();

        when(userRepository.findByOauthProviderAndOauthProviderId(provider, ID)).thenReturn(Optional.empty());
        when(userRepository.findByUsernameOrEmail(username, email)).thenReturn(new ArrayList<>());
        when(tokenService.createToken(any(), any())).thenReturn(token);
        Cookie cookie = new Cookie("confirmationToken", token.getToken());
        when(cookieService.generateConfirmationCookie(token)).thenReturn(cookie);

        MockHttpServletResponse response = new MockHttpServletResponse();

        oAuthHandler.onAuthenticationSuccess(new MockHttpServletRequest(), response, oAuthToken);

        verify(userRepository, times(1)).save(userArgumentCaptor.capture());

        User savedPendingUser = userArgumentCaptor.getValue();
        checkCreatedUser(savedPendingUser, name, email, username, provider, response, getConfirmationUrl(username), List.of(cookie));
    }

    private Entry<OAuth2AuthenticationToken, OAuth2User> mockGoogleAuthentication() {
        var entry = mockAuthentication("google", ID, EMAIL);
        when(entry.getSecond().getAttribute("name")).thenReturn(NAME);
        return entry;
    }

    private Entry<OAuth2AuthenticationToken, OAuth2User> mockGithubAuthentication() {
        var entry = mockAuthentication("github", ID, null);
        when(entry.getSecond().getAttribute("name")).thenReturn(NAME);
        when(entry.getSecond().getAttribute("login")).thenReturn(USERNAME);
        return entry;
    }

    private Entry<OAuth2AuthenticationToken, OAuth2User> mockFacebookAuthentication() {
        var entry = mockAuthentication("facebook", ID, null);
        when(entry.getSecond().getAttribute("name")).thenReturn(NAME);
        return entry;
    }

    private Entry<OAuth2AuthenticationToken, OAuth2User> mockDiscordAuthentication() {
        var entry = mockAuthentication("discord", ID, EMAIL);
        when(entry.getSecond().getAttribute("username")).thenReturn(USERNAME);
        return entry;
    }

    private Entry<OAuth2AuthenticationToken, OAuth2User> mockAuthentication(String provider, String id, String email) {
        var entry = mockAuthentication(provider, id);
        when(entry.getFirst().getPrincipal()).thenReturn(entry.getSecond());
        when(entry.getSecond().getAttribute("email")).thenReturn(email);
        return entry;
    }

    private Entry<OAuth2AuthenticationToken, OAuth2User> mockAuthentication(String provider, String id) {
        OAuth2AuthenticationToken oauthToken = mock(OAuth2AuthenticationToken.class);
        OAuth2User principal = mock(OAuth2User.class);
        when(oauthToken.getAuthorizedClientRegistrationId()).thenReturn(provider);
        when(oauthToken.getName()).thenReturn(id);
        return new Entry<>(oauthToken, principal);
    }

    private void checkCreatedUser(User savedPendingUser, String name, String email,
                                  String username, OAuthProvider oauthProvider,
                                  MockHttpServletResponse response, String expectedUrl, List<Cookie> expectedCookies) {
        assertEquals(name, savedPendingUser.getName());
        assertEquals(email, savedPendingUser.getEmail());
        assertEquals(username, savedPendingUser.getUsername());
        assertEquals(oauthProvider, savedPendingUser.getOauthProvider());
        assertEquals(ID, savedPendingUser.getOauthProviderId());
        assertFalse(savedPendingUser.getActive());
        assertEquals(expectedUrl, response.getRedirectedUrl());
        checkCookies(expectedCookies, response);
    }

    private void checkCookies(List<Cookie> expectedCookies, MockHttpServletResponse response) {
        if (expectedCookies != null && !expectedCookies.isEmpty()) {
            for (Cookie expectedCookie : expectedCookies) {
                Cookie actualCookie = response.getCookie(expectedCookie.getName());
                assertNotNull(actualCookie);
                assertEquals(expectedCookie.getValue(), actualCookie.getValue());
            }
        }
    }

    private String getConfirmationUrl(String username) {
        String url = config.getClientUrl() + "/confirmation";
        if (username != null) {
            url += "?username=" + username;
        }
        return url;
    }

    @Getter
    @AllArgsConstructor
    static class Entry<F, S> {
        F first;
        S second;
    }
}
