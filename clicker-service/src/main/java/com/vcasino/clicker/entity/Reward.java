package com.vcasino.clicker.entity;

import com.vcasino.clicker.config.IntegratedService;
import com.vcasino.clicker.entity.enums.RewardType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Entity
@Table(name = "reward")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Reward {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Integer id;

    @Column(name = "type", nullable = false)
    @Enumerated(EnumType.STRING)
    RewardType type;

    @Column(name = "name", nullable = false)
    String name;

    @Column(name = "link")
    String link;

    @Column(name = "duration_seconds")
    Integer durationInSeconds;

    @Column(name = "service_name", nullable = false)
    @Enumerated(EnumType.STRING)
    IntegratedService integratedService;

    @Column(name = "reward_coins", nullable = false)
    Integer rewardCoins;

    @Column(name = "valid_from", nullable = false)
    LocalDateTime validFrom;

    @Column(name = "ends_in")
    LocalDateTime endsIn;

    @PrePersist
    @PreUpdate
    private void validateLink() {
        if ((type == RewardType.WATCH || type == RewardType.SUBSCRIBE) && link == null) {
            throw new IllegalArgumentException("Link must not be null when type is WATCH or SUBSCRIBE");
        }

        if (type == RewardType.WATCH && (durationInSeconds == null || durationInSeconds <= 0)) {
            throw new IllegalArgumentException("Duration must be not null and greater than zero when type is WATCH");
        }
    }
}
