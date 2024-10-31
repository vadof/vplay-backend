package com.vcasino.clicker.entity;

import com.vcasino.clicker.entity.id.key.AccountRewardKey;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
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
@Table(name = "account_rewards_received")
@Getter
@Setter
@IdClass(AccountRewardKey.class)
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AccountRewardsReceived {
    @Id
    @Column(name = "account_id")
    private Long accountId;

    @Id
    @Column(name = "reward_id")
    private Integer rewardId;

    @Column(name = "received_at", nullable = false)
    private LocalDateTime receivedAt;
}
