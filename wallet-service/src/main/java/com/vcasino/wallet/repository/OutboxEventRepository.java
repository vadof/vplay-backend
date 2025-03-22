package com.vcasino.wallet.repository;

import com.vcasino.wallet.entity.OutboxEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface OutboxEventRepository extends JpaRepository<OutboxEvent, UUID> {

    @Query(value = """
        SELECT * FROM (
            SELECT id, aggregate_id, type, payload, status, applicant, created_at, modified_at, version,
                 ROW_NUMBER() OVER (PARTITION BY applicant ORDER BY created_at ASC) as rn
            FROM outbox_event
            WHERE status = :status AND created_at < :createdAt
        ) sub
        WHERE rn <= :recordsPerApplicant
    """, nativeQuery = true)
    List<OutboxEvent> findPerApplicantByStatusAndCreatedAtBefore(
            @Param("status") String status,
            @Param("createdAt") Instant createdAt,
            @Param("recordsPerApplicant") Integer recordsPerApplicant
    );
}
