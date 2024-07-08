package com.vcasino.user.entity;


import com.vcasino.entities.Country;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;

@Entity
@Table(name = "my_user")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class User {

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

    @JoinColumn(nullable = false, name = "country_code")
    @ManyToOne(fetch = FetchType.EAGER)
    private Country country;

    @Column(columnDefinition = "DECIMAL(14,2)")
    private BigDecimal balance;

    @Column(columnDefinition = "DECIMAL(14,2)")
    private BigDecimal profit;

    @DateTimeFormat(pattern="dd/MM/yyyy")
    private LocalDate registerDate;

    @Enumerated(EnumType.STRING)
    private Role role;

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
