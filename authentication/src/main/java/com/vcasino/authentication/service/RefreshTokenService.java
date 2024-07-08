package com.vcasino.authentication.service;

import com.vcasino.authentication.entity.RefreshToken;
import com.vcasino.authentication.entity.User;
import com.vcasino.authentication.exception.AppException;
import com.vcasino.authentication.repository.RefreshTokenRepository;
import com.vcasino.authentication.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
public class RefreshTokenService {
    @Value("${jwt.refreshExpirationMs}")
    private Long refreshExpiration;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private UserRepository userRepository;

    public RefreshToken findByToken(String token) {
        return refreshTokenRepository.findByToken(token)
                .orElseThrow(() -> new AppException("Refresh token not found", HttpStatus.UNAUTHORIZED));
    }

    public RefreshToken createRefreshToken(Long userId) {
        User user = userRepository.findById(userId).get();

        RefreshToken refreshToken;
        Optional<RefreshToken> tokenOptional = refreshTokenRepository.findByUser(user);

        if (tokenOptional.isPresent()) {
            refreshToken = tokenOptional.get();
        } else {
            refreshToken = new RefreshToken();
            refreshToken.setUser(user);
        }

        refreshToken.setExpiryDate(Instant.now().plusMillis(refreshExpiration));
        refreshToken.setToken(UUID.randomUUID().toString());

        refreshToken = refreshTokenRepository.save(refreshToken);
        return refreshToken;
    }

    public RefreshToken verifyExpiration(RefreshToken token) {
        if (token.getExpiryDate().compareTo(Instant.now()) < 0) {
            refreshTokenRepository.delete(token);
            throw new AppException("Refresh token was expired. Please make a new login request", HttpStatus.UNAUTHORIZED);
        }

        return token;
    }
}
