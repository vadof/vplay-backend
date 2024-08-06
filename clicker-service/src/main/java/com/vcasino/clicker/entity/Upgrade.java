package com.vcasino.clicker.entity;

import com.vcasino.clicker.entity.id.key.UpgradeKey;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
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

@Entity
@Table(name = "upgrade")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@IdClass(UpgradeKey.class)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Upgrade {
    @Id
    @Column(name = "name")
    String name;

    @Id
    @Column(name = "level")
    Integer level;

    @ManyToOne
    @JoinColumn(name = "section", referencedColumnName = "name")
    Section section;

    @Column(name = "profit_per_hour")
    Integer profitPerHour;

    @Column(name = "profit_per_hour_delta")
    Integer profitPerHourDelta;

    @Column(name = "price_to_upgrade")
    Integer priceToUpdate;

    @ManyToOne
    @JoinColumn(name = "condition_id")
    Condition condition;

    @Column(name = "max_level", nullable = false)
    Boolean maxLevel;
}
