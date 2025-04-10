package com.vcasino.bet.service;

import com.vcasino.bet.dto.RegisterTournamentRequest;
import com.vcasino.bet.entity.Tournament;
import com.vcasino.bet.exception.AppException;
import com.vcasino.bet.repository.TournamentRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
@Slf4j
public class TournamentService {

    private final TournamentRepository tournamentRepository;

    public Tournament addTournament(RegisterTournamentRequest request) {
        if (!request.getStartDate().isBefore(request.getEndDate())) {
            throw new AppException("Start date must be before end date", HttpStatus.BAD_REQUEST);
        }

        if (tournamentRepository.existsByTitleAndDiscipline(request.getTitle(), request.getDiscipline())) {
            throw new AppException("Tournament with title %s in %s discipline already exists"
                    .formatted(request.getTitle(), request.getDiscipline()), HttpStatus.BAD_REQUEST);
        }

        Tournament tournament = Tournament.builder()
                .title(request.getTitle())
                .discipline(request.getDiscipline())
                .tournamentPage(request.getTournamentPage())
                .image(null) // TODO
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .build();

        return tournamentRepository.save(tournament);
    }

}
