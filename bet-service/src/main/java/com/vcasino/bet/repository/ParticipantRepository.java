package com.vcasino.bet.repository;

import com.vcasino.bet.entity.Participant;
import com.vcasino.bet.entity.enums.Discipline;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ParticipantRepository extends JpaRepository<Participant, Integer> {
    Optional<Participant> findByNameAndDiscipline(String name, Discipline discipline);
    boolean existsByNameAndDiscipline(String name, Discipline discipline);
}
