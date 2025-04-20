package com.vcasino.bet.service;

import com.vcasino.bet.dto.request.RegisterTournamentRequest;
import com.vcasino.bet.entity.Tournament;
import com.vcasino.bet.exception.AppException;
import com.vcasino.bet.repository.TournamentRepository;
import com.vcasino.bet.service.image.ImageStorageService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
@Slf4j
public class TournamentService {

    private final TournamentRepository tournamentRepository;
    private final ImageStorageService imageStorageService;

    public Tournament addTournament(RegisterTournamentRequest request) {
        if (!request.getStartDate().isBefore(request.getEndDate())) {
            throw new AppException("Start date must be before end date", HttpStatus.BAD_REQUEST);
        }

        if (tournamentRepository.existsByTitleAndDiscipline(request.getTitle(), request.getDiscipline())) {
            throw new AppException("Tournament with title %s in %s discipline already exists"
                    .formatted(request.getTitle(), request.getDiscipline()), HttpStatus.BAD_REQUEST);
        }

        if (!imageStorageService.existsByKey(request.getImageKey())) {
            throw new AppException("Image not found", HttpStatus.NOT_FOUND);
        }

        Tournament tournament = Tournament.builder()
                .title(request.getTitle())
                .discipline(request.getDiscipline())
                .tournamentPage(request.getTournamentPage())
                .image(request.getImageKey())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .build();

        return tournamentRepository.save(tournament);
    }

}
