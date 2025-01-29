package com.vcasino.user.repository;

import com.vcasino.user.entity.OAuthProvider;
import com.vcasino.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    @Query("SELECT u FROM User u WHERE u.email = :email OR u.username = :username")
    List<User> findByUsernameOrEmail(@Param("username") String username, @Param("email") String email);

    Optional<User> findByOauthProviderAndOauthProviderId(OAuthProvider oAuthProvider, String oAuthProviderId);

}
