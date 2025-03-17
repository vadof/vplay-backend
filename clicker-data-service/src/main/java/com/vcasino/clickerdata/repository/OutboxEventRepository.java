package com.vcasino.clickerdata.repository;

import com.vcasino.clickerdata.entity.EventStatus;
import com.vcasino.clickerdata.entity.EventType;
import com.vcasino.clickerdata.entity.OutboxEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface OutboxEventRepository extends JpaRepository<OutboxEvent, UUID> {

    List<OutboxEvent> findAllByEventTypeAndStatusAndCreatedAtBefore(EventType eventType, EventStatus status, Instant date);

    @Transactional
    @Modifying
    @Query("UPDATE OutboxEvent o SET o.status = :status, o.modifiedAt = CURRENT_TIMESTAMP WHERE o.id = :id")
    void updateStatusById(UUID id, EventStatus status);
}
