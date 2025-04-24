package com.vcasino.bet.repository;

import com.vcasino.bet.entity.Participant;
import com.vcasino.bet.entity.enums.Discipline;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ParticipantRepository extends JpaRepository<Participant, Integer> {
    Optional<Participant> findByNameAndDiscipline(String name, Discipline discipline);

    @Query("SELECT p.name FROM Participant p WHERE p.discipline = :discipline")
    List<String> findAllNamesByDiscipline(@Param("discipline") Discipline discipline);
}
