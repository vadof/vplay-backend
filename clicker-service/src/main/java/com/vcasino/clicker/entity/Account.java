package com.vcasino.clicker.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.List;

@Entity
@Table(name = "account")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    Long id;

    @Column(name = "user_id", nullable = false, unique = true)
    Long userId;

    @Column(name = "level", nullable = false)
    Integer level;

    @Column(name = "net_worth", nullable = false, columnDefinition = "DECIMAL(21,3)")
    BigDecimal netWorth;

    @Column(name = "balance_coins", nullable = false, columnDefinition = "DECIMAL(21,3)")
    BigDecimal balanceCoins;

    @Column(name = "available_taps", nullable = false)
    Integer availableTaps;

    @Column(name = "max_taps", nullable = false)
    Integer maxTaps;

    @Column(name = "earn_per_tap", nullable = false)
    Integer earnPerTap;

    @Column(name = "taps_recover_per_sec", nullable = false)
    Integer tapsRecoverPerSec;

    @Column(name = "passive_earn_per_hour", nullable = false)
    Integer passiveEarnPerHour;

    @Column(name = "last_sync_date", nullable = false)
    Timestamp lastSyncDate;

    @Column(name = "suspicious_actions_number", nullable = false)
    Integer suspiciousActionsNumber;

    @Column(name = "frozen", nullable = false)
    Boolean frozen;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "account_upgrade",
            joinColumns = @JoinColumn(name = "account_id"),
            inverseJoinColumns = {
                    @JoinColumn(name = "upgrade_name", referencedColumnName = "name"),
                    @JoinColumn(name = "upgrade_level", referencedColumnName = "level")
            }
    )
    List<Upgrade> upgrades;
}
