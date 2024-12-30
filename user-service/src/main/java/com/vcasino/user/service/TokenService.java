package com.vcasino.user.service;

import com.vcasino.user.entity.Token;
import com.vcasino.user.entity.TokenType;
import com.vcasino.user.entity.User;
import com.vcasino.user.exception.AppException;
import com.vcasino.user.repository.TokenRepository;
import com.vcasino.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
public class TokenService {
    @Value("${jwt.refreshExpirationMs}")
    private Long jwtRefreshExpiration;

    @Value("${verification.token.expirationMs}")
    private Long verificationTokenExpiration;

    @Autowired
    private TokenRepository tokenRepository;

    @Autowired
    private UserRepository userRepository;

    public Token findByToken(String token, TokenType type) {
        return tokenRepository.findByTokenAndType(token, type)
                .orElseThrow(() -> new AppException("Token not found", HttpStatus.FORBIDDEN));
    }

    public Token createToken(Long userId, TokenType tokenType) {
        User user = userRepository.findById(userId).get();

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

        if (tokenType.equals(TokenType.VERIFICATION)) {
            token.setExpiryDate(Instant.now().plusMillis(verificationTokenExpiration));
        } else if (tokenType.equals(TokenType.REFRESH)) {
            token.setExpiryDate(Instant.now().plusMillis(jwtRefreshExpiration));
        }


        token = tokenRepository.save(token);
        return token;
    }

    public void deleteToken(User user) {
        tokenRepository.deleteByUser(user);
    }

    public Token verifyExpiration(Token token) {
        if (token.getExpiryDate().compareTo(Instant.now()) < 0) {
            tokenRepository.delete(token);
            throw new AppException("Token expired. Please make a new request", HttpStatus.FORBIDDEN);
        }

        return token;
    }
}
