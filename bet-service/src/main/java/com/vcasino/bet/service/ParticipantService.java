package com.vcasino.bet.service;

import com.vcasino.bet.dto.RegisterParticipantRequest;
import com.vcasino.bet.entity.Participant;
import com.vcasino.bet.exception.AppException;
import com.vcasino.bet.repository.ParticipantRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
@Slf4j
public class ParticipantService {

    private final ParticipantRepository participantRepository;

    public Participant addParticipant(RegisterParticipantRequest request) {

        if (participantRepository.findByNameAndDiscipline(request.getName(), request.getDiscipline()).isPresent()) {
            throw new AppException("Participant already exists", HttpStatus.BAD_REQUEST);
        }

        Participant participant = Participant.builder()
                .name(request.getName())
                .shortName(Strings.isBlank(request.getShortName()) ? request.getName() : request.getShortName())
                .discipline(request.getDiscipline())
                .image(null) // TODO
                .participantPage(request.getParticipantPage())
                .build();

        return participantRepository.save(participant);
    }
}
