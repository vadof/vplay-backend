package com.vcasino.user.repository;

import com.vcasino.user.entity.Token;
import com.vcasino.user.entity.TokenType;
import com.vcasino.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TokenRepository extends JpaRepository<Token, Long> {
    Optional<Token> findByTokenAndType(String token, TokenType type);

    Optional<Token> findByUser(User user);

    Optional<Token> findByUserAndType(User user, TokenType type);
}
