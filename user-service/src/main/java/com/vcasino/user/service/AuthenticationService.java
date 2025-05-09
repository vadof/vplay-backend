package com.vcasino.user.service;

import com.vcasino.commonkafka.enums.Topic;
import com.vcasino.commonkafka.event.UserCreateEvent;
import com.vcasino.user.config.ApplicationConfig;
import com.vcasino.user.config.securiy.JwtService;
import com.vcasino.user.dto.UserDto;
import com.vcasino.user.dto.auth.AuthenticationRequest;
import com.vcasino.user.dto.auth.AuthenticationResponse;
import com.vcasino.user.dto.auth.TokenRefreshRequest;
import com.vcasino.user.dto.auth.TokenRefreshResponse;
import com.vcasino.user.dto.email.EmailTokenOptionsDto;
import com.vcasino.user.entity.Role;
import com.vcasino.user.entity.Token;
import com.vcasino.user.entity.TokenType;
import com.vcasino.user.entity.User;
import com.vcasino.user.exception.AppException;
import com.vcasino.user.mapper.UserMapper;
import com.vcasino.user.repository.UserRepository;
import com.vcasino.user.utils.RegexUtil;
import jakarta.annotation.Nullable;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.regex.Pattern;

import static com.vcasino.user.utils.EmailTemplate.buildEmailConfirmationTemplate;

@Service
@AllArgsConstructor
@Slf4j
public class AuthenticationService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    private final TokenService tokenService;
    private final EmailService emailService;
    private final CookieService cookieService;

    private final ApplicationConfig applicationConfig;

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Transactional
    public EmailTokenOptionsDto registerUser(UserDto userDto, String ref) {
        User user = validateUserFields(userDto, Role.USER);
        setInvitedBy(user, ref);
        userRepository.save(user);

        Token confirmationToken = tokenService.createEmailConfirmationToken(user);

        log.info("Pending User#{} saved to database", user.getId());

        EmailTokenOptionsDto options = tokenService.getEmailTokenOptionsDtoFromToken(confirmationToken);
        options.setEmail(user.getEmail());
        options.setCanResend(true);

        sendEmailConfirmation(user.getEmail(), confirmationToken.getToken());

        return options;
    }

    public void registerAdmin(UserDto userDto, @Nullable User registeredBy) {
        User user = validateUserFields(userDto, Role.ADMIN);

        user.setInvitedBy(registeredBy);
        userRepository.save(user);

        log.info("Admin#{} saved to database", user.getId());
        sendUserCreated(user.getId(), user.getUsername(), registeredBy);
    }

    private User validateUserFields(UserDto userDto, Role role) {
        boolean admin = role.equals(Role.ADMIN);
        if (admin) {
            validatePassword(userDto.getPassword());
        } else {
            checkForInvalidCharacters(userDto.getPassword());
        }

        validateEmail(userDto.getEmail());
        validateUsername(userDto.getUsername());

        User user = userMapper.toEntity(userDto);
        user.setRegisterDate(Instant.now());
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRole(role);
        user.setFrozen(false);
        user.setActive(admin);

        return user;
    }

    @Transactional(noRollbackFor = AppException.class)
    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));

        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new AppException("Unauthorized access", HttpStatus.UNAUTHORIZED));

        if (!user.getActive()) {
            Token token = tokenService.findByUserAndType(user, TokenType.EMAIL_CONFIRMATION)
                    .orElseThrow(() -> {
                        log.warn("Inactive User#{} without a token", user.getId());
                        userRepository.delete(user);
                        return new AppException(null, HttpStatus.INTERNAL_SERVER_ERROR);
                    });

            if (tokenService.isTokenExpired(token)) {
                userRepository.delete(user);
                throw new AppException("Invalid Username or Password!", HttpStatus.UNAUTHORIZED);
            } else {
                throw new AppException("Check " + user.getEmail() + " for an email to complete your account setup", HttpStatus.UNAUTHORIZED);
            }
        }

        String jwtToken = jwtService.generateJwtToken(user);
        Token refreshToken = tokenService.createToken(user, TokenType.REFRESH);

        return new AuthenticationResponse(jwtToken, refreshToken.getToken(), userMapper.toDto(user), null);
    }

    @Transactional(noRollbackFor = AppException.class)
    public AuthenticationResponse confirmUsername(String username, String confirmationToken, String ref) {
        return processConfirmation(
                confirmationToken,
                TokenType.USERNAME_CONFIRMATION,
                true,
                user -> {
                    if (!username.equals(user.getUsername())) {
                        validateUsername(username);
                        user.setUsername(username);
                    }
                    setInvitedBy(user, ref);
                }
        );
    }

    @Transactional(noRollbackFor = AppException.class)
    public AuthenticationResponse confirmEmail(String confirmationToken) {
        return processConfirmation(
                confirmationToken,
                TokenType.EMAIL_CONFIRMATION,
                false,
                user -> {
                }
        );
    }

    private AuthenticationResponse processConfirmation(
            String confirmationToken,
            TokenType tokenType,
            boolean includeHeaders,
            Consumer<User> userUpdater
    ) {
        Token token = tokenService.findByTokenAndType(confirmationToken, tokenType);
        User user = token.getUser();

        if (tokenService.isTokenExpired(token)) {
            userRepository.delete(user);
            throw new AppException("Registration time has expired, please complete it again", HttpStatus.FORBIDDEN);
        }

        if (user.getActive()) {
            log.warn("Active User#{} with confirmation token", user.getId());
            throw new AppException(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }

        userUpdater.accept(user);

        user.setModifiedAt(Instant.now());
        user.setActive(true);
        userRepository.save(user);

        String jwtToken = jwtService.generateJwtToken(user);
        Token refreshToken = tokenService.createToken(user, TokenType.REFRESH);

        log.info("User#{} has activated the account", user.getId());

        sendUserCreated(user.getId(), user.getUsername(), user.getInvitedBy());

        HttpHeaders headers = null;
        if (includeHeaders) {
            headers = new HttpHeaders();
            headers.add(HttpHeaders.SET_COOKIE, cookieService.resetConfirmationCookie().toString());
        }

        return new AuthenticationResponse(jwtToken, refreshToken.getToken(), userMapper.toDto(user), headers);
    }

    public void sendUserCreated(Long id, String username, @Nullable User invitedBy) {
        log.info("Send user-create event for \"{}\"", username);
        Long invitedById = invitedBy == null ? null : invitedBy.getId();
        kafkaTemplate.send(Topic.USER_CREATE.getName(), new UserCreateEvent(id, username, invitedById));
    }

    @Transactional(noRollbackFor = AppException.class)
    public EmailTokenOptionsDto resendConfirmationEmail(EmailTokenOptionsDto emailTokenOptions) {
        User user = userRepository.findByEmail(emailTokenOptions.getEmail())
                .orElseThrow(() -> new AppException(null, HttpStatus.FORBIDDEN));

        Token token = tokenService.findByUserAndType(user, TokenType.EMAIL_CONFIRMATION).orElseThrow(
                () -> new AppException(null, HttpStatus.FORBIDDEN));

        if (tokenService.isTokenExpired(token)) {
            userRepository.delete(user);
            throw new AppException("Registration time has expired, please complete it again.", HttpStatus.FORBIDDEN);
        }

        Token.EmailTokenOptions options = tokenService.getEmailTokenOptionsFromToken(token);

        if (!options.getResendToken().equals(emailTokenOptions.getResendToken())) {
            log.warn("Unauthorized access to send an email to {}", user.getEmail());
            throw new AppException(null, HttpStatus.FORBIDDEN);
        }

        int maxEmailsNumber = 3;
        long emailResendIntervalMultiplier = 30L;

        if (options.getEmailsSent() >= maxEmailsNumber) {
            String logStr = "The limit for sending letters to %s has been reached".formatted(user.getEmail());
            log.info(logStr);
            throw new AppException(logStr + " , please wait or contact support.", HttpStatus.BAD_REQUEST);
        }

        Instant now = Instant.now();
        if (options.getSentAt().plusSeconds(emailResendIntervalMultiplier * options.getEmailsSent()).isAfter(now)) {
            throw new AppException("Please wait before resending the email.", HttpStatus.BAD_REQUEST);
        }

        options.setEmailsSent(options.getEmailsSent() + 1);
        options.setSentAt(Instant.now());
        tokenService.updateTokenOptions(token, options);

        sendEmailConfirmation(user.getEmail(), token.getToken());

        return new EmailTokenOptionsDto(user.getEmail(), options.getResendToken(),
                options.getEmailsSent(), options.getEmailsSent() != maxEmailsNumber);
    }

    @Transactional
    public void deletePendingUser(EmailTokenOptionsDto emailTokenOptions) {
        User user = userRepository.findByEmail(emailTokenOptions.getEmail())
                .orElseThrow(() -> new AppException(null, HttpStatus.FORBIDDEN));

        Token token = tokenService.findByUserAndType(user, TokenType.EMAIL_CONFIRMATION).orElseThrow(
                () -> new AppException(null, HttpStatus.FORBIDDEN));

        Token.EmailTokenOptions options = tokenService.getEmailTokenOptionsFromToken(token);

        if (!options.getResendToken().equals(emailTokenOptions.getResendToken())) {
            log.warn("Unauthorized access to clear pending user {}", user.getEmail());
            throw new AppException(null, HttpStatus.FORBIDDEN);
        }

        userRepository.delete(user);
    }

    private void sendEmailConfirmation(String email, String confirmationToken) {
        String confirmationUrl = "%s/register/confirmation?type=email&confirmationToken=%s"
                .formatted(applicationConfig.getClientUrl(), confirmationToken);

        emailService.send(email, buildEmailConfirmationTemplate(confirmationUrl));
    }

    @Transactional
    public TokenRefreshResponse refreshToken(TokenRefreshRequest request) {
        Token refreshToken = tokenService.findByTokenAndType(request.getRefreshToken(), TokenType.REFRESH);

        Long secondsBeforeExpiration = tokenService.getSecondsBeforeTokenExpiration(refreshToken, null);

        int twoHours = 7200;
        boolean generateNewRefreshToken = secondsBeforeExpiration <= twoHours;
        if (generateNewRefreshToken) {
            refreshToken = tokenService.renewToken(refreshToken);
        }

        String token = jwtService.generateJwtToken(refreshToken.getUser());

        return new TokenRefreshResponse(token, refreshToken.getToken());
    }

    private void setInvitedBy(User user, String ref) {
        if (Strings.isNotEmpty(ref) && !ref.equals(user.getUsername())) {
            Optional<User> optionalUser = userRepository.findByUsername(ref);
            if (optionalUser.isEmpty()) {
                log.warn("Referral User with {} username not found", ref);
            } else {
                User invitedByUser = optionalUser.get();
                if (invitedByUser.getActive() && !invitedByUser.getFrozen()) {
                    user.setInvitedBy(invitedByUser);
                }
            }
        }
    }

    private void validateUsername(String username) {
        if (!username.matches(RegexUtil.USERNAME_REGEX)) {
            throw new AppException("Username can only contain english letters, numbers and underscores", HttpStatus.BAD_REQUEST);
        }

        Optional<User> optionalUser = userRepository.findByUsername(username);
        if (optionalUser.isPresent()) {
            User user = optionalUser.get();
            if (user.getActive() || !deleteUserIfNotActive(user)) {
                throw new AppException("Username already exists", HttpStatus.BAD_REQUEST);
            }
        }
    }

    private void validateEmail(String email) {
        Optional<User> optionalUser = userRepository.findByEmail(email);
        if (optionalUser.isPresent()) {
            User user = optionalUser.get();
            if (user.getActive() || !deleteUserIfNotActive(user)) {
                throw new AppException("Email already in use", HttpStatus.BAD_REQUEST);
            }
        }
    }

    private boolean deleteUserIfNotActive(User user) {
        long tokenExpirationInSeconds = applicationConfig.getConfirmation().getToken().getExpirationMs() / 1000;
        if (Instant.now().minusSeconds(tokenExpirationInSeconds).isAfter(user.getRegisterDate())) {
            userRepository.delete(user);
            return true;
        }
        return false;
    }

    private void validatePassword(String password) {
        if (password.length() < 12) {
            throw new AppException("Min password length is 12 symbols", HttpStatus.BAD_REQUEST);
        }

        checkForInvalidCharacters(password);

        if (!passwordContainsSpecialSymbol(password)) {
            throw new AppException("Password must contain at least 1 special symbol", HttpStatus.BAD_REQUEST);
        }

        if (!passwordContainsCapitalLetter(password)) {
            throw new AppException("Password must contain at least 1 capital letter", HttpStatus.BAD_REQUEST);
        }

        if (!passwordContainsNumber(password)) {
            throw new AppException("Password must contain at least 1 number", HttpStatus.BAD_REQUEST);
        }
    }

    private void checkForInvalidCharacters(String password) {
        String invalidChars = getInvalidCharacters(password);
        if (!invalidChars.isEmpty()) {
            throw new AppException("Password cannot contain: " + invalidChars, HttpStatus.BAD_REQUEST);
        }
    }

    private String getInvalidCharacters(String password) {
        StringBuilder invalidChars = new StringBuilder();
        for (char ch : password.toCharArray()) {
            if (!String.valueOf(ch).matches(RegexUtil.PASSWORD_REGEX)) {
                invalidChars.append(ch);
            }
        }
        return invalidChars.toString();
    }

    private boolean passwordContainsSpecialSymbol(String password) {
        Pattern r = Pattern.compile(RegexUtil.SPECIAL_SYMBOL_REGEX);
        return r.matcher(password).find();
    }

    private boolean passwordContainsCapitalLetter(String password) {
        return password.matches(".*" + RegexUtil.CAPITAL_LETTER_REGEX + ".*");
    }

    private boolean passwordContainsNumber(String password) {
        return password.matches(".*" + RegexUtil.NUMBER_REGEX + ".*");
    }
}
