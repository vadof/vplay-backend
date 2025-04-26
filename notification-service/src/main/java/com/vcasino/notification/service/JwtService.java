package com.vcasino.notification.service;

import com.vcasino.notification.config.ApplicationConfig;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Date;
import java.util.function.Function;

@Service
@RequiredArgsConstructor
@Slf4j
public class JwtService {

    private final ApplicationConfig applicationConfig;
    private PublicKey publicKey;

    @PostConstruct
    private void getRsaKeys() throws Exception {
        Path pbPath = Paths.get(applicationConfig.getJwt().getPublicKeyPath());

        if (Files.exists(pbPath)) {
            log.info("Public key found");

            byte[] publicKeyBytes = Files.readAllBytes(pbPath);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            X509EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(publicKeyBytes);

            publicKey = keyFactory.generatePublic(publicKeySpec);
        } else {
            log.error("Public key not found");
            System.exit(1);
        }
    }

    private Claims extractClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(publicKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private <T> T extractClaim(Claims claims, Function<Claims, T> claimsResolver) {
        return claimsResolver.apply(claims);
    }

    public String validateTokenAndGetUserId(String token) throws Exception {
        Claims claims = extractClaims(token);
        Date expiration = extractClaim(claims, Claims::getExpiration);
        boolean tokenExpired = expiration.before(new Date());
        if (tokenExpired) {
            throw new Exception("Token expired");
        }

        return String.valueOf(claims.get("id"));
    }
}
