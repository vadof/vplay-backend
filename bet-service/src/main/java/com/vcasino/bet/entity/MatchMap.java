package com.vcasino.bet.entity;

import com.vcasino.bet.entity.key.MatchMapKey;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
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
import lombok.ToString;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "match_map")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@IdClass(MatchMapKey.class)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class MatchMap {

    @Id
    @Column(name = "match_id")
    Long matchId;

    @Id
    @Column(name = "map_number", nullable = false)
    Integer mapNumber;

    @Column(name = "map_name")
    String mapName;

    @Column(name = "participant1_score", nullable = false)
    Integer participant1Score;

    @Column(name = "participant2_score", nullable = false)
    Integer participant2Score;

    @Column(name = "initial_wp_1", columnDefinition = "DECIMAL(4,3)")
    Double initialWP1;

    @Column(name = "initial_wp_2", columnDefinition = "DECIMAL(4,3)")
    Double initialWP2;

    @Column(name = "current_wp_1", columnDefinition = "DECIMAL(4,3)")
    Double currentWP1;

    @Column(name = "current_wp_2", columnDefinition = "DECIMAL(4,3)")
    Double currentWP2;

    @Column(name = "winner")
    Integer winner;

    @UpdateTimestamp
    @Column(name = "updated_at")
    LocalDateTime updatedAt;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "match_id", insertable = false, updatable = false)
    Match match;
}
