package com.vcasino.user.oauth2;

import com.vcasino.user.config.ApplicationConfig;
import com.vcasino.user.config.securiy.JwtService;
import com.vcasino.user.entity.OAuthProvider;
import com.vcasino.user.entity.Token;
import com.vcasino.user.entity.Role;
import com.vcasino.user.entity.TokenType;
import com.vcasino.user.entity.User;
import com.vcasino.user.repository.TokenRepository;
import com.vcasino.user.repository.UserRepository;
import com.vcasino.user.service.CookieService;
import com.vcasino.user.service.TokenService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Component
@AllArgsConstructor
@Slf4j
public class OAuth2LoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final TokenService tokenService;
    private final CookieService cookieService;

    private final ApplicationConfig config;
    private final TokenRepository tokenRepository;

    @Override
    @Transactional
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        OAuth2AuthenticationToken oauthToken = (OAuth2AuthenticationToken) authentication;

        String providerId = oauthToken.getName();
        OAuthProvider provider = OAuthProvider.valueOf(oauthToken.getAuthorizedClientRegistrationId().toUpperCase());

        log.info("OAuth success {}: {}", provider, providerId);

        Optional<User> optionalUser = userRepository.findByOauthProviderAndOauthProviderId(provider, providerId);
        if (optionalUser.isPresent()) {
            User user = optionalUser.get();
            if (user.getActive()) {
                authenticateUser(response, user);
            } else {
                Token token = tokenService.createToken(user, TokenType.CONFIRMATION);
                redirectUserToConfirmation(response, user, token);
            }
        } else {
            log.info("{} user registration", provider);
            if (provider.equals(OAuthProvider.GOOGLE)) {
                handleGoogleRegistration(response, oauthToken);
            } else if (provider.equals(OAuthProvider.FACEBOOK)) {
                handleFacebookRegistration(response, oauthToken);
            } else if (provider.equals(OAuthProvider.DISCORD)) {
                handleDiscordRegistration(response, oauthToken);
            } else if (provider.equals(OAuthProvider.GITHUB)) {
                handleGithubRegistration(response, oauthToken);
            }
        }
    }

    private void handleGoogleRegistration(HttpServletResponse response, OAuth2AuthenticationToken oauthToken) throws IOException {
        String providerId = oauthToken.getName();
        OAuth2User principal = oauthToken.getPrincipal();
        String name = principal.getAttribute("name");
        String email = principal.getAttribute("email");
        String possibleUsername = email.substring(0, email.indexOf("@"));

        processRegistration(response, providerId, OAuthProvider.GOOGLE, name, email, possibleUsername);
    }

    private void handleGithubRegistration(HttpServletResponse response, OAuth2AuthenticationToken oauthToken) throws IOException {
        String providerId = oauthToken.getName();
        OAuth2User principal = oauthToken.getPrincipal();
        String name = principal.getAttribute("name");
        String possibleUsername = principal.getAttribute("login");
        String email = principal.getAttribute("email");

        processRegistration(response, providerId, OAuthProvider.GITHUB, name, email, possibleUsername);
    }

    private void handleFacebookRegistration(HttpServletResponse response, OAuth2AuthenticationToken oauthToken) throws IOException {
        String providerId = oauthToken.getName();
        OAuth2User principal = oauthToken.getPrincipal();
        String name = principal.getAttribute("name");
        String email = principal.getAttribute("email");
        String possibleUsername = name.replaceAll(" ", "_");

        processRegistration(response, providerId, OAuthProvider.FACEBOOK, name, email, possibleUsername);
    }

    private void handleDiscordRegistration(HttpServletResponse response, OAuth2AuthenticationToken oauthToken) throws IOException {
        String providerId = oauthToken.getName();
        OAuth2User principal = oauthToken.getPrincipal();
        String possibleUsername = principal.getAttribute("username");
        String email = principal.getAttribute("email");

        processRegistration(response, providerId, OAuthProvider.DISCORD, null, email, possibleUsername);
    }

    private void processRegistration(HttpServletResponse response, String providerId, OAuthProvider provider, String name, String email, String possibleUsername) throws IOException {
        List<User> users = userRepository.findByUsernameOrEmail(possibleUsername, email);

        String username = null;
        if (users.stream().noneMatch(user -> user.getUsername().equals(possibleUsername))) {
            username = possibleUsername;
        }

        if (email == null) {
            createPendingUserAndRedirect(response, providerId, provider, name, null, username);
        } else {
            Optional<User> userByEmail = users.stream().filter(user -> user.getEmail().equals(email)).findFirst();
            if (userByEmail.isEmpty()) {
                createPendingUserAndRedirect(response, providerId, provider, name, email, username);
            } else {
                User user = userByEmail.get();
                if (user.getActive()) {
                    log.info("Active User#{} with email {} already exists, connecting oauth", user.getId(), email);
                    if (user.getOauthProvider() == null) {
                        user.setOauthProvider(provider);
                        user.setOauthProviderId(providerId);
                        user.setModifiedAt(LocalDateTime.now());
                        userRepository.save(user);
                    }
                    authenticateUser(response, user);
                } else {
                    log.info("Deleting inactive User#{} with email {}", user.getId(), email);
                    tokenRepository.deleteByUser(user);
                    userRepository.delete(user);

                    if (possibleUsername.equals(user.getUsername())) {
                        username = possibleUsername;
                    }

                    createPendingUserAndRedirect(response, providerId, provider, name, email, username);
                }
            }
        }
    }

    private void createPendingUserAndRedirect(HttpServletResponse response, String providerId, OAuthProvider provider, String name, String email, String username) throws IOException {
        User user = User.builder()
                .name(name)
                .email(email)
                .username(username)
                .oauthProvider(provider)
                .oauthProviderId(providerId)
                .registerDate(LocalDateTime.now())
                .role(Role.USER)
                .active(false)
                .frozen(false)
                .build();

        userRepository.save(user);

        log.info("Pending User#{} saved to database", user.getId());

        Token token = tokenService.createToken(user, TokenType.CONFIRMATION);
        redirectUserToConfirmation(response, user, token);
    }

    private void redirectUserToConfirmation(HttpServletResponse response, User user, Token token) throws IOException {
        log.info("Redirecting User#{} to confirmation", user.getId());
        String url = config.getClientUrl() + "/confirmation";
        if (user.getUsername() != null) {
            url += "?username=" + user.getUsername();
        }
        response.addCookie(cookieService.generateConfirmationCookie(token));
        response.sendRedirect(url);
    }

    private void authenticateUser(HttpServletResponse response, User user) throws IOException {
        log.info("Authenticating User#{}", user.getId());
        String jwtToken = jwtService.generateToken(user);
        Token refreshToken = tokenService.createToken(user, TokenType.REFRESH);
        String url = "%s/login/success?name=%s&username=%s&email=%s"
                .formatted(config.getClientUrl(), user.getName(), user.getUsername(), user.getEmail());
        response.addCookie(cookieService.generateJwtCookie(jwtToken));
        response.addCookie(cookieService.generateJwtRefreshCookie(refreshToken));
        response.sendRedirect(url);
    }
}
