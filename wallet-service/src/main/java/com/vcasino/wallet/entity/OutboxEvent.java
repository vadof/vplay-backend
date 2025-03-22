package com.vcasino.wallet.entity;

import com.vcasino.wallet.entity.enums.Applicant;
import com.vcasino.wallet.entity.enums.EventStatus;
import com.vcasino.wallet.entity.enums.EventType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "outbox_event")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OutboxEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false)
    UUID id;

    @Column(name = "aggregate_id", nullable = false)
    Long aggregateId;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    EventType type;

    @Column(name = "payload", nullable = false, columnDefinition = "TEXT")
    String payload;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    EventStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "applicant", nullable = false)
    Applicant applicant;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false, nullable = false)
    Instant createdAt;

    @Column(name = "modified_at")
    Instant modifiedAt;

    @Version
    @Column(name = "version", nullable = false)
    Integer version;
}
