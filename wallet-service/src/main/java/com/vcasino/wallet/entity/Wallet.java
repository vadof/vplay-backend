package com.vcasino.wallet.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "wallet")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Wallet {
    @Id
    @Column(name = "id")
    Long id;

    @Column(name = "balance", nullable = false, columnDefinition = "DECIMAL(14,2)")
    BigDecimal balance;

    @Column(name = "reserved", nullable = false, columnDefinition = "DECIMAL(14,2)")
    BigDecimal reserved;

    @Column(name = "updated_at", nullable = false)
    Instant updatedAt;

    @Column(name = "frozen", nullable = false)
    Boolean frozen;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "invited_by", referencedColumnName = "id")
    Wallet invitedBy;

    @OneToOne(mappedBy = "wallet", fetch = FetchType.LAZY, orphanRemoval = true)
    ReferralBonus referralBonus;

    @Version
    @Column(name = "version", nullable = false)
    Integer version;
}
