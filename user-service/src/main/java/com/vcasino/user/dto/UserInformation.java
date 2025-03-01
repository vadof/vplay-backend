package com.vcasino.user.dto;

import com.vcasino.user.entity.OAuthProvider;
import com.vcasino.user.entity.Role;
import com.vcasino.user.entity.User;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserInformation {
    Long id;
    String name;
    String username;
    String email;
    String password;
    Role role;
    OAuthProvider oauthProvider;
    String oauthProviderId;
    User invitedBy;
    Instant registerDate;
    Instant modifiedAt;
    Boolean active;
    Boolean frozen;
}
