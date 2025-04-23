package com.vcasino.bet.entity;

import com.vcasino.bet.entity.enums.TransactionType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "transaction")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Transaction {
    @Id
    @Column(name = "event_id")
    UUID eventId;

    @Column(name = "amount")
    BigDecimal amount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bet_id", referencedColumnName = "bet_id", nullable = false)
    Bet bet;

    @Enumerated(value = EnumType.STRING)
    @Column(name = "transaction_type", nullable = false)
    TransactionType type;
}
