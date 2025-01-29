package com.vcasino.user.config.securiy;

import com.vcasino.user.config.ApplicationConfig;
import com.vcasino.user.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

@Service
@Slf4j
public class JwtService {

    private final ApplicationConfig.JwtProperties jwtConfig;

    public JwtService(ApplicationConfig constantsConfig) {
        this.jwtConfig = constantsConfig.getJwt();
    }

    private PrivateKey privateKey;
    private PublicKey publicKey;

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    public String generateJwtToken(User user) {
        List<String> roles = user.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList();
        return generateJwtToken(Map.of("roles", roles, "id", user.getId()), user);
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        return extractUsername(token).equals(userDetails.getUsername())
                && !isTokenExpired(token);
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public String generateJwtToken(Map<String, Object> extractClaims, UserDetails userDetails) {
        return Jwts.builder()
                .setClaims(extractClaims)
                .setSubject(userDetails.getUsername())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + jwtConfig.getExpirationMs()))
                .signWith(privateKey, SignatureAlgorithm.RS256)
                .compact();
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(publicKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    @PostConstruct
    private void generateRsaKeys() throws Exception {
        Path privateKeyPath = Paths.get( jwtConfig.getKeysPath() + "/private.key");
        Path publicKeyPath = Paths.get(jwtConfig.getKeysPath() + "/public.key");

        if (Files.exists(privateKeyPath) && Files.exists(publicKeyPath)) {
            log.info("RSA keys found");
            byte[] privateKeyBytes = Files.readAllBytes(privateKeyPath);
            byte[] publicKeyBytes = Files.readAllBytes(publicKeyPath);

            KeyFactory keyFactory = KeyFactory.getInstance("RSA");

            PKCS8EncodedKeySpec privateKeySpec = new PKCS8EncodedKeySpec(privateKeyBytes);
            X509EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(publicKeyBytes);

            privateKey = keyFactory.generatePrivate(privateKeySpec);
            publicKey = keyFactory.generatePublic(publicKeySpec);
        } else {
            log.info("RSA keys not found, generating...");
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
            keyPairGenerator.initialize(4096);
            KeyPair keyPair = keyPairGenerator.generateKeyPair();

            privateKey = keyPair.getPrivate();
            publicKey = keyPair.getPublic();

            Files.write(privateKeyPath, keyPair.getPrivate().getEncoded());
            Files.write(publicKeyPath, keyPair.getPublic().getEncoded());

            log.info("RSA keys generated and written");
        }
    }

}
