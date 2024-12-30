package com.vcasino.user.ouath2;

import com.vcasino.user.config.securiy.JwtService;
import com.vcasino.user.entity.OAuthProvider;
import com.vcasino.user.entity.Token;
import com.vcasino.user.entity.Role;
import com.vcasino.user.entity.TokenType;
import com.vcasino.user.entity.User;
import com.vcasino.user.repository.UserRepository;
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
    // TODO with .env
    private final static String FRONTEND_URL = "http://localhost:4200";

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        OAuth2AuthenticationToken oauthToken = (OAuth2AuthenticationToken) authentication;

        String providerId = oauthToken.getName();
        OAuthProvider provider = OAuthProvider.valueOf(oauthToken.getAuthorizedClientRegistrationId().toUpperCase());

        Optional<User> optionalUser = userRepository.findByOauthProviderAndOauthProviderId(provider, providerId);
        if (optionalUser.isPresent()) {
            User user = optionalUser.get();
            if (user.getActive()) {
                authenticateUser(response, user);
            } else {
                redirectUserToConfirmation(response, user);
            }
        } else {
            if (provider.equals(OAuthProvider.GOOGLE)) {
                handleGoogleRegistration(response, oauthToken);
            } else if (provider.equals(OAuthProvider.GITHUB)) {
                handleGithubRegistration(response, oauthToken);
            }
        }
    }

    private void handleGoogleRegistration(HttpServletResponse response, OAuth2AuthenticationToken oauthToken) throws IOException {
        System.out.println("Handle google registration");
        String providerId = oauthToken.getName();
        OAuth2User principal = oauthToken.getPrincipal();
        String name = principal.getAttribute("name");
        String email = principal.getAttribute("email");
        String possibleUsername = email.substring(0, email.indexOf("@"));

        processRegistration(response, providerId, OAuthProvider.GOOGLE, name, email, possibleUsername);
    }

    private void handleGithubRegistration(HttpServletResponse response, OAuth2AuthenticationToken oauthToken) throws IOException {
        System.out.println("Handle github registration");
        String providerId = oauthToken.getName();
        OAuth2User principal = oauthToken.getPrincipal();
        String name = principal.getAttribute("name");
        String possibleUsername = principal.getAttribute("login");
        String email = principal.getAttribute("email");

        processRegistration(response, providerId, OAuthProvider.GITHUB, name, email, possibleUsername);
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
            Optional<User> optionalUser = users.stream().filter(user -> user.getEmail().equals(email)).findFirst();
            if (optionalUser.isEmpty()) {
                createPendingUserAndRedirect(response, providerId, provider, name, email, username);
            } else {
                User user = optionalUser.get();
                if (user.getActive()) {
                    if (user.getOauthProvider() == null) {
                        user.setOauthProvider(provider);
                        user.setOauthProviderId(providerId);
                        userRepository.save(user);
                    }
                    authenticateUser(response, user);
                } else {
                    userRepository.delete(user);
                }

                authenticateUser(response, user);
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
        redirectUserToConfirmation(response, user);
    }

    private void redirectUserToConfirmation(HttpServletResponse response, User user) throws IOException {
        System.out.println("REDIRECT TO USER CONFIRMATION");
        Token token = tokenService.createToken(user.getId(), TokenType.VERIFICATION);
        String url = "%s/login/confirmation?token=%s".formatted(FRONTEND_URL, token.getToken());
        System.out.println("TO " + url);
        response.sendRedirect(url);
    }

    private void authenticateUser(HttpServletResponse response, User user) throws IOException {
        System.out.println("REDIRECT TO AUTHENTICATE USER CONFIRMATION");
        String jwtToken = jwtService.generateToken(user);
        Token refreshToken = tokenService.createToken(user.getId(), TokenType.REFRESH);

        String url = "%s/login/success?token=%s&refreshToken=%s".formatted(FRONTEND_URL, jwtToken, refreshToken);
        System.out.println("TO " + url);
        response.sendRedirect(url);
    }
}
