package com.vcasino.clicker.entity;

import jakarta.persistence.Cacheable;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

@Entity
@Table(name = "level")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@Cacheable
@Cache(usage = CacheConcurrencyStrategy.READ_ONLY)
public class Level {
    @Id
    @Column(name = "value")
    Integer value;
    
    @Column(name = "name", nullable = false, unique = true)
    String name;
    
    @Column(name = "net_worth", nullable = false, unique = true)
    Long netWorth;

    @Column(name = "earn_per_tap", nullable = false)
    Integer earnPerTap;

    @Column(name = "taps_recover_per_sec", nullable = false)
    Integer tapsRecoverPerSec;

    @Column(name = "max_taps", nullable = false)
    Integer maxTaps;
}
