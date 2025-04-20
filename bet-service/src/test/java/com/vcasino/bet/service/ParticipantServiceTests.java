package com.vcasino.bet.service;


import com.vcasino.bet.dto.request.RegisterParticipantRequest;
import com.vcasino.bet.entity.Participant;
import com.vcasino.bet.entity.enums.Discipline;
import com.vcasino.bet.exception.AppException;
import com.vcasino.bet.repository.ParticipantRepository;
import com.vcasino.bet.service.image.ImageStorageService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * UNIT tests for {@link ParticipantService}
 */
@ExtendWith(MockitoExtension.class)
public class ParticipantServiceTests {

    @Mock
    ImageStorageService imageStorageService;
    @Mock
    ParticipantRepository participantRepository;

    @InjectMocks
    ParticipantService participantService;

    @Captor
    ArgumentCaptor<Participant> argumentCaptor;

    @Test
    @DisplayName("Add participant")
    void testAddParticipant_Success() {
        RegisterParticipantRequest request = new RegisterParticipantRequest("Team", null,
                Discipline.COUNTER_STRIKE, "participants/image.webp", "https://some-page_team.com");

        when(participantRepository.findByNameAndDiscipline(request.getName(), request.getDiscipline()))
                .thenReturn(Optional.empty());
        when(imageStorageService.existsByKey(request.getImageKey())).thenReturn(true);

        participantService.addParticipant(request);
        verify(participantRepository, times(1)).save(argumentCaptor.capture());
        Participant participant = argumentCaptor.getValue();

        assertEquals(request.getName(), participant.getName());
        assertEquals(request.getName(), participant.getShortName());
        assertEquals(request.getDiscipline(), participant.getDiscipline());
        assertEquals(request.getImageKey(), participant.getImage());
        assertEquals(request.getParticipantPage(), participant.getParticipantPage());
    }

    @Test
    @DisplayName("Add participant, participant with same name and discipline already exists")
    void testAddParticipant_ParticipantExists() {
        RegisterParticipantRequest request = new RegisterParticipantRequest("Team", null,
                Discipline.COUNTER_STRIKE, "participants/image.webp", "https://some-page_team.com");

        when(participantRepository.findByNameAndDiscipline(request.getName(), request.getDiscipline()))
                .thenReturn(Optional.of(new Participant()));


        assertThrows(AppException.class, () -> participantService.addParticipant(request));
    }

    @Test
    @DisplayName("Add participant image not found")
    void testAddParticipant_ImageNotFound() {
        RegisterParticipantRequest request = new RegisterParticipantRequest("Team", null,
                Discipline.COUNTER_STRIKE, "participants/image.webp", "https://some-page_team.com");

        when(participantRepository.findByNameAndDiscipline(request.getName(), request.getDiscipline()))
                .thenReturn(Optional.empty());
        when(imageStorageService.existsByKey(request.getImageKey())).thenReturn(false);

        assertThrows(AppException.class, () -> participantService.addParticipant(request));
    }
}
