package com.vcasino.user.service;

import com.vcasino.user.entity.RefreshToken;
import com.vcasino.user.entity.User;
import com.vcasino.user.exception.AppException;
import com.vcasino.user.repository.RefreshTokenRepository;
import com.vcasino.user.repository.UserRepository;
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
    private Long jwtRefreshExpiration;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private UserRepository userRepository;

    public RefreshToken findByToken(String token) {
        return refreshTokenRepository.findByToken(token)
                .orElseThrow(() -> new AppException("Refresh token not found", HttpStatus.FORBIDDEN));
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

        refreshToken.setExpiryDate(Instant.now().plusMillis(jwtRefreshExpiration));
        refreshToken.setToken(UUID.randomUUID().toString());

        refreshToken = refreshTokenRepository.save(refreshToken);
        return refreshToken;
    }

    public RefreshToken verifyExpiration(RefreshToken token) {
        if (token.getExpiryDate().compareTo(Instant.now()) < 0) {
            refreshTokenRepository.delete(token);
            throw new AppException("Refresh token was expired. Please make a new login request", HttpStatus.FORBIDDEN);
        }

        return token;
    }
}
