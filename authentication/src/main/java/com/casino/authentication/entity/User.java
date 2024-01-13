package com.casino.authentication.entity;


import jakarta.persistence.*;
import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "my_user")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String firstname;

    @Column(nullable = false)
    private String lastname;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @JoinColumn(nullable = false, name = "country_id")
    @ManyToOne(fetch = FetchType.EAGER)
    private Country country;

    @JoinColumn(nullable = false, name = "currency_id")
    @ManyToOne(fetch = FetchType.EAGER)
    private Currency currency;

    @Column(columnDefinition = "DECIMAL(14,2)")
    private BigDecimal balance;

    @DateTimeFormat(pattern="dd/MM/yyyy")
    private LocalDate registerDate;

    @Enumerated(EnumType.STRING)
    private Role role;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(role.name()));
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User otherUser)) return false;
        return Objects.equals(username, otherUser.username);
    }

    @Override
    public int hashCode() {
        return Objects.hash(username);
    }

}
