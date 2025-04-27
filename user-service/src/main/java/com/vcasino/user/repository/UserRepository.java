package com.vcasino.user.repository;

import com.vcasino.user.entity.OAuthProvider;
import com.vcasino.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    @Query("SELECT u FROM User u WHERE u.email = :email OR u.username = :username")
    List<User> findByUsernameOrEmail(@Param("username") String username, @Param("email") String email);

    Optional<User> findByOauthProviderAndOauthProviderId(OAuthProvider oAuthProvider, String oAuthProviderId);

    @Query(nativeQuery = true, value = """
    SELECT
        COUNT(*) AS registeredUsers,
        COUNT(CASE WHEN u.oauth_provider IS NOT NULL THEN 1 END) AS registeredUsersWithOAuth,
        COUNT(CASE WHEN u.invited_by IS NOT NULL THEN 1 END) AS registeredUsersInvitedByOthers,
        COUNT(CASE WHEN u.register_date::date = CURRENT_DATE THEN 1 END) AS registeredUsersToday,
        COUNT(CASE WHEN u.register_date >= CURRENT_DATE - INTERVAL '7' DAY THEN 1 END) AS registeredUsersLastWeek,
        COUNT(CASE WHEN u.register_date >= CURRENT_DATE - INTERVAL '30' DAY THEN 1 END) AS registeredUsersLastMonth,
        COUNT(CASE WHEN u.active = TRUE THEN 1 END) AS activeUsers,
        COUNT(CASE WHEN u.frozen = TRUE THEN 1 END) AS frozenUsers
    FROM my_user u
    """)
    UserGeneralStatistics fetchUserStatistics();

    interface UserGeneralStatistics {
        long getRegisteredUsers();
        long getRegisteredUsersWithOAuth();
        long getRegisteredUsersInvitedByOthers();
        long getRegisteredUsersToday();
        long getRegisteredUsersLastWeek();
        long getRegisteredUsersLastMonth();
        long getActiveUsers();
        long getFrozenUsers();
    }

    @Query(nativeQuery = true, value = """
    SELECT
        u.id as userId,
        u.username as username,
        u.register_date as registerDate
    FROM my_user u
    WHERE u.active = TRUE
    ORDER BY u.register_date DESC
    LIMIT 20;
    """)
    List<UserRegistrationStatistics> fetchRegistrationStatistics();

    interface UserRegistrationStatistics {
        long getUserId();
        String getUsername();
        String getRegisterDate();
    }

    @Query(nativeQuery = true, value = """
    SELECT
        u.id as id,
        u.name as name,
        u.username as username,
        u.email as email,
        u.role as role,
        u.oauth_provider as oAuthProvider,
        inviter.username as invitedBy,
        (SELECT COUNT(*) FROM my_user WHERE invited_by = u.id) as usersInvited,
        u.register_date as registerDate,
        inviter.username as invitedBy,
        u.register_date as registerDate,
        u.active as active,
        u.frozen as frozen
    FROM my_user u
        LEFT JOIN my_user inviter ON u.invited_by = inviter.id
    WHERE u.id = :id
    """)
    UserInformation fetchUserInformation(@Param("id") Long id);

    @Query(nativeQuery = true, value = """
    SELECT
        u.id as id,
        u.name as name,
        u.username as username,
        u.email as email,
        u.role as role,
        u.oauth_provider as oAuthProvider,
        inviter.username as invitedBy,
        (SELECT COUNT(*) FROM my_user WHERE invited_by = u.id) as usersInvited,
        u.register_date as registerDate,
        inviter.username as invitedBy,
        u.register_date as registerDate,
        u.active as active,
        u.frozen as frozen
    FROM my_user u
        LEFT JOIN my_user inviter ON u.invited_by = inviter.id
    WHERE u.username = :username
    """)
    UserInformation fetchUserInformation(@Param("username") String username);

    interface UserInformation {
        Long getId();
        String getName();
        String getUsername();
        String getEmail();
        String getRole();
        String getOAuthProvider();
        String getInvitedBy();
        Long getUsersInvited();
        Instant getRegisterDate();
        Boolean getActive();
        Boolean getFrozen();
    }

}
