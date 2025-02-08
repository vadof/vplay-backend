package com.vcasino.clicker.repository;

import com.vcasino.clicker.entity.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface TaskRepository extends JpaRepository<Task, Integer> {

    @Query("FROM Task t WHERE t.validFrom <= :now AND (t.endsIn IS NULL OR t.endsIn >= :now)")
    List<Task> findAllInInterval(@Param("now") LocalDateTime now);
}
