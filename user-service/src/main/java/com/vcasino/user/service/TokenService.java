package com.vcasino.user.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vcasino.user.config.ApplicationConfig;
import com.vcasino.user.dto.email.EmailTokenOptionsDto;
import com.vcasino.user.entity.Token;
import com.vcasino.user.entity.TokenType;
import com.vcasino.user.entity.User;
import com.vcasino.user.exception.AppException;
import com.vcasino.user.repository.TokenRepository;
import jakarta.annotation.Nullable;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
@AllArgsConstructor
@Slf4j
public class TokenService {
    private final ApplicationConfig config;

    private final TokenRepository tokenRepository;
    private final ObjectMapper objectMapper;

    public Token findByTokenAndType(String token, TokenType type) {
        return tokenRepository.findByTokenAndType(token, type)
                .orElseThrow(() -> new AppException("Token not found", HttpStatus.FORBIDDEN));
    }

    public Token createToken(User user, TokenType tokenType) {
        Token token;
        Optional<Token> tokenOptional = tokenRepository.findByUser(user);

        if (tokenOptional.isPresent()) {
            token = tokenOptional.get();
            token.setOptions(null);
        } else {
            token = new Token();
            token.setUser(user);
        }

        token.setType(tokenType);
        token.setToken(UUID.randomUUID().toString());

        if (tokenType.equals(TokenType.REFRESH)) {
            token.setExpiryDate(Instant.now().plusMillis(config.getJwt().getRefreshExpirationMs()));
        } else if (tokenType.equals(TokenType.USERNAME_CONFIRMATION)) {
            token.setExpiryDate(Instant.now().plusMillis(config.getConfirmation().getToken().getExpirationMs()));
        }

        return tokenRepository.save(token);
    }

    public Token createEmailConfirmationToken(User user) {
        Token token = new Token();
        token.setUser(user);

        token.setType(TokenType.EMAIL_CONFIRMATION);
        token.setToken(UUID.randomUUID().toString());
        token.setExpiryDate(Instant.now().plusMillis(config.getConfirmation().getToken().getExpirationMs()));

        try {
            Token.EmailTokenOptions options = new Token.EmailTokenOptions(UUID.randomUUID().toString(), 1, Instant.now());
            token.setOptions(objectMapper.writeValueAsString(options));
        } catch (JsonProcessingException e) {
            log.error("Error during converting email token options to string", e);
            throw new AppException(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }

        return tokenRepository.save(token);
    }

    public Token.EmailTokenOptions getEmailTokenOptionsFromToken(Token token) {
        try {
            return objectMapper.readValue(token.getOptions(), Token.EmailTokenOptions.class);
        } catch (JsonProcessingException e) {
            log.error("Error during converting token options", e);
            throw new AppException(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public EmailTokenOptionsDto getEmailTokenOptionsDtoFromToken(Token token) {
        try {
            return objectMapper.readValue(token.getOptions(), EmailTokenOptionsDto.class);
        } catch (JsonProcessingException e) {
            log.error("Error during converting token options", e);
            throw new AppException(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public Optional<Token> findByUserAndType(User user, TokenType type) {
        return tokenRepository.findByUserAndType(user, type);
    }

    public void updateTokenOptions(Token token, Token.EmailTokenOptions options) {
        try {
            token.setOptions(objectMapper.writeValueAsString(options));
        } catch (JsonProcessingException e) {
            log.error("Error during converting email token options to string", e);
            throw new AppException(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }

        tokenRepository.save(token);
    }

    public void deleteToken(Token token) {
        tokenRepository.delete(token);
    }

    public void deleteTokenByUser(User user) {
        tokenRepository.deleteByUser(user);
    }

    public boolean isTokenExpired(Token token) {
        return token.getExpiryDate().compareTo(Instant.now()) < 0;
    }

    public Token verifyExpiration(Token token, @Nullable String errorMessage) {
        if (token.getExpiryDate().compareTo(Instant.now()) < 0) {
            deleteToken(token);
            if (errorMessage == null) {
                errorMessage = "Token expired. Please make a new request";
            }
            throw new AppException(errorMessage, HttpStatus.FORBIDDEN);
        }

        return token;
    }
}
