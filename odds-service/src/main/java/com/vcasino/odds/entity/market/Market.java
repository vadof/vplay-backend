package com.vcasino.odds.entity.market;

import com.vcasino.odds.entity.Match;
import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorColumn;
import jakarta.persistence.DiscriminatorType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "market")
@Getter
@Setter
@NoArgsConstructor
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "dtype", discriminatorType = DiscriminatorType.STRING)
@FieldDefaults(level = AccessLevel.PROTECTED)
public abstract class Market {

    @Id
    @Column(name = "market_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "match_id", referencedColumnName = "match_id")
    Match match;

    @Column(name = "outcome", nullable = false, columnDefinition = "DECIMAL(3,1)")
    BigDecimal outcome;

    @Column(name = "odds", nullable = false, columnDefinition = "DECIMAL(5,2)")
    BigDecimal odds;

    @Column(name = "closed", nullable = false)
    Boolean closed = false;

    @Enumerated(value = EnumType.STRING)
    @Column(name = "result")
    MarketResult result;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false)
    LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    LocalDateTime updatedAt;

    public Market(Match match, BigDecimal outcome, BigDecimal odds) {
        this.match = match;
        this.outcome = outcome;
        this.odds = odds;
    }
}