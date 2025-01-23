package com.vcasino.user.service;

import com.vcasino.user.config.ApplicationConfig;
import com.vcasino.user.entity.Token;
import com.vcasino.user.entity.TokenType;
import com.vcasino.user.entity.User;
import com.vcasino.user.exception.AppException;
import com.vcasino.user.repository.TokenRepository;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
@AllArgsConstructor
public class TokenService {
    private final ApplicationConfig config;

    private final TokenRepository tokenRepository;

    public Token findByTokenAndType(String token, TokenType type) {
        return tokenRepository.findByTokenAndType(token, type)
                .orElseThrow(() -> new AppException("Token not found", HttpStatus.FORBIDDEN));
    }

    public Token createToken(User user, TokenType tokenType) {
        Token token;
        Optional<Token> tokenOptional = tokenRepository.findByUser(user);

        if (tokenOptional.isPresent()) {
            token = tokenOptional.get();
        } else {
            token = new Token();
            token.setUser(user);
        }

        token.setType(tokenType);
        token.setToken(UUID.randomUUID().toString());

        if (tokenType.equals(TokenType.CONFIRMATION)) {
            token.setExpiryDate(Instant.now().plusMillis(config.getConfirmation().getToken().getExpirationMs()));
        } else if (tokenType.equals(TokenType.REFRESH)) {
            token.setExpiryDate(Instant.now().plusMillis(config.getJwt().getRefreshExpirationMs()));
        }

        return tokenRepository.save(token);
    }

    public void deleteToken(Token token) {
        tokenRepository.delete(token);
    }

    public Token verifyExpiration(Token token) {
        if (token.getExpiryDate().compareTo(Instant.now()) < 0) {
            deleteToken(token);
            throw new AppException("Token expired. Please make a new request", HttpStatus.FORBIDDEN);
        }

        return token;
    }

}
