package com.vcasino.bet.service;

import com.vcasino.bet.dto.request.RegisterParticipantRequest;
import com.vcasino.bet.entity.Participant;
import com.vcasino.bet.entity.enums.Discipline;
import com.vcasino.bet.exception.AppException;
import com.vcasino.bet.repository.ParticipantRepository;
import com.vcasino.bet.service.image.ImageStorageService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
@Slf4j
public class ParticipantService {

    private final ImageStorageService imageStorageService;
    private final ParticipantRepository participantRepository;

    public Participant addParticipant(RegisterParticipantRequest request) {

        if (participantRepository.findByNameAndDiscipline(request.getName(), request.getDiscipline()).isPresent()) {
            throw new AppException("Participant already exists", HttpStatus.BAD_REQUEST);
        }

        if (!imageStorageService.existsByKey(request.getImageKey())) {
            throw new AppException("Image not found", HttpStatus.NOT_FOUND);
        }

        Participant participant = Participant.builder()
                .name(request.getName())
                .shortName(Strings.isBlank(request.getShortName()) ? request.getName() : request.getShortName())
                .discipline(request.getDiscipline())
                .image(request.getImageKey())
                .participantPage(request.getParticipantPage())
                .build();

        return participantRepository.save(participant);
    }

    public List<String> getParticipants(Discipline discipline) {
        return participantRepository.findAllNamesByDiscipline(discipline);
    }
}
