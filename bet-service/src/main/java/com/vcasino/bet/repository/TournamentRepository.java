package com.vcasino.bet.repository;

import com.vcasino.bet.entity.Tournament;
import com.vcasino.bet.entity.enums.Discipline;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TournamentRepository extends JpaRepository<Tournament, Integer> {
    boolean existsByTitleAndDiscipline(String title, Discipline discipline);
}
