package com.vcasino.bet.entity;

import com.vcasino.bet.entity.enums.MatchStatus;
import com.vcasino.bet.entity.market.Market;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "match")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Match {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "match_id")
    Long id;

    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    @JoinColumn(name = "tournament_id", referencedColumnName = "tournament_id")
    Tournament tournament;

    @Column(name = "match_page", nullable = false)
    String matchPage;

    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    @JoinColumn(name = "participant1_id", referencedColumnName = "participant_id")
    Participant participant1;

    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    @JoinColumn(name = "participant2_id", referencedColumnName = "participant_id")
    Participant participant2;

    @Column(name = "format", nullable = false)
    String format;

    @OneToMany(mappedBy = "match", orphanRemoval = true, fetch = FetchType.EAGER)
    List<MatchMap> matchMaps;

    @Column(name = "start_date", nullable = false)
    LocalDateTime startDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    MatchStatus status;

    @Column(name = "winner")
    Integer winner;

    @Column(name = "win_probability1", columnDefinition = "DECIMAL(3,2)")
    Double winProbability1;

    @Column(name = "win_probability2", columnDefinition = "DECIMAL(3,2)")
    Double winProbability2;

    @OneToMany(mappedBy = "match", fetch = FetchType.EAGER)
    List<Market> markets;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "modified_at")
    LocalDateTime modifiedAt;
}
