package com.vcasino.bet.service;


import com.vcasino.bet.client.MarketInitializationRequest;
import com.vcasino.bet.client.OddsClient;
import com.vcasino.bet.dto.request.RegisterMatchRequest;
import com.vcasino.bet.dto.response.MarketsByCategory;
import com.vcasino.bet.dto.response.MatchDto;
import com.vcasino.bet.dto.response.TournamentDto;
import com.vcasino.bet.entity.Match;
import com.vcasino.bet.entity.MatchMap;
import com.vcasino.bet.entity.market.Market;
import com.vcasino.bet.exception.AppException;
import com.vcasino.bet.mapper.MarketMapper;
import com.vcasino.bet.mapper.MarketMapperImpl;
import com.vcasino.bet.mapper.MatchMapper;
import com.vcasino.bet.mapper.MatchMapperImpl;
import com.vcasino.bet.mapper.ParticipantMapper;
import com.vcasino.bet.mapper.ParticipantMapperImpl;
import com.vcasino.bet.mapper.TournamentMapper;
import com.vcasino.bet.mapper.TournamentMapperImpl;
import com.vcasino.bet.mock.MarketMocks;
import com.vcasino.bet.mock.MatchMocks;
import com.vcasino.bet.repository.MatchRepository;
import com.vcasino.bet.repository.ParticipantRepository;
import com.vcasino.bet.repository.TournamentRepository;
import com.vcasino.commonkafka.event.MatchUpdateEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * UNIT tests for {@link MatchService}
 */
@ExtendWith(MockitoExtension.class)
public class MatchServiceTests {

    @Mock
    MatchRepository matchRepository;
    @Mock
    TournamentRepository tournamentRepository;
    @Mock
    ParticipantRepository participantRepository;
    @Mock
    OddsClient oddsClient;
    @Spy
    TournamentMapper tournamentMapper = new TournamentMapperImpl();
    @Spy
    MatchMapper matchMapper = new MatchMapperImpl();
    @Spy
    MarketMapper marketMapper = new MarketMapperImpl();
    @Mock
    RedisService redisService;

    @InjectMocks
    MatchService matchService;

    @BeforeEach
    void iniMapperDependencies() {
        ParticipantMapper participantMapper = new ParticipantMapperImpl();

        ReflectionTestUtils.setField(matchMapper, "participantMapper", participantMapper);
        ReflectionTestUtils.setField(matchMapper, "marketMapper", marketMapper);
    }

    private RegisterMatchRequest getRequest(Match matchMock) {
        return new RegisterMatchRequest(matchMock.getTournament().getId(),
                matchMock.getMatchPage(), matchMock.getParticipant1().getName(), matchMock.getParticipant2().getName(),
                matchMock.getFormat(), BigDecimal.valueOf(matchMock.getWinProbability1()),
                BigDecimal.valueOf(matchMock.getWinProbability2()), matchMock.getStartDate());
    }

    @Test
    @DisplayName("Add match")
    void testAddMatch_Success() {
        Match matchMock = MatchMocks.getMatchMock(1L);
        RegisterMatchRequest request = getRequest(matchMock);

        when(tournamentRepository.findById(request.getTournamentId())).thenReturn(Optional.of(matchMock.getTournament()));
        when(matchRepository.existsByMatchPage(request.getMatchPage())).thenReturn(false);
        when(matchRepository.save(any(Match.class))).thenReturn(matchMock);
        when(participantRepository.findByNameAndDiscipline(request.getParticipant1(), matchMock.getTournament().getDiscipline()))
                .thenReturn(Optional.of(matchMock.getParticipant1()));
        when(participantRepository.findByNameAndDiscipline(request.getParticipant2(), matchMock.getTournament().getDiscipline()))
                .thenReturn(Optional.of(matchMock.getParticipant2()));

        matchService.addMatch(request);

        verify(matchRepository, times(1)).save(any(Match.class));
        verify(oddsClient, times(1)).initializeMarkets(any());
    }

    @Test
    @DisplayName("Add match tournament not found")
    void testAddMatch_TournamentNotFound() {
        Match matchMock = MatchMocks.getMatchMock(1L);
        RegisterMatchRequest request = getRequest(matchMock);

        when(tournamentRepository.findById(request.getTournamentId())).thenReturn(Optional.empty());

        assertThrows(AppException.class, () -> matchService.addMatch(request));
    }

    @Test
    @DisplayName("Add match match with same page already exists")
    void testAddMatch_MatchWithSamePageAlreadyExists() {
        Match matchMock = MatchMocks.getMatchMock(1L);
        RegisterMatchRequest request = getRequest(matchMock);

        when(tournamentRepository.findById(request.getTournamentId())).thenReturn(Optional.of(matchMock.getTournament()));
        when(matchRepository.existsByMatchPage(request.getMatchPage())).thenReturn(true);

        assertThrows(AppException.class, () -> matchService.addMatch(request));
    }

    @Test
    @DisplayName("Add match invalid format")
    void testAddMatch_InvalidFormat() {
        Match matchMock = MatchMocks.getMatchMock(1L);
        RegisterMatchRequest request = getRequest(matchMock);
        request.setFormat("2B");

        when(tournamentRepository.findById(request.getTournamentId())).thenReturn(Optional.of(matchMock.getTournament()));
        when(matchRepository.existsByMatchPage(request.getMatchPage())).thenReturn(false);
        when(participantRepository.findByNameAndDiscipline(request.getParticipant1(), matchMock.getTournament().getDiscipline()))
                .thenReturn(Optional.of(matchMock.getParticipant1()));
        when(participantRepository.findByNameAndDiscipline(request.getParticipant2(), matchMock.getTournament().getDiscipline()))
                .thenReturn(Optional.of(matchMock.getParticipant2()));

        assertThrows(AppException.class, () -> matchService.addMatch(request));
    }

    @Test
    @DisplayName("Add match invalid win probabilities")
    void testAddMatch_InvalidWinProbabilities() {
        Match matchMock = MatchMocks.getMatchMock(1L);
        RegisterMatchRequest request = getRequest(matchMock);
        request.setWinProbability1(BigDecimal.valueOf(0.1));
        request.setWinProbability1(BigDecimal.valueOf(0.2));

        when(tournamentRepository.findById(request.getTournamentId())).thenReturn(Optional.of(matchMock.getTournament()));
        when(matchRepository.existsByMatchPage(request.getMatchPage())).thenReturn(false);

        assertThrows(AppException.class, () -> matchService.addMatch(request));
    }

    @Test
    @DisplayName("Add match invalid start date")
    void testAddMatch_InvalidStartDate() {
        Match matchMock = MatchMocks.getMatchMock(1L);
        RegisterMatchRequest request = getRequest(matchMock);
        request.setStartDate(LocalDateTime.now());

        when(tournamentRepository.findById(request.getTournamentId())).thenReturn(Optional.of(matchMock.getTournament()));
        when(matchRepository.existsByMatchPage(request.getMatchPage())).thenReturn(false);

        assertThrows(AppException.class, () -> matchService.addMatch(request));
    }

    @Test
    @DisplayName("Add match market initialization failed")
    void testAddMatch_MarketInitializationFailed() {
        Match matchMock = MatchMocks.getMatchMock(1L);
        RegisterMatchRequest request = getRequest(matchMock);

        when(tournamentRepository.findById(request.getTournamentId())).thenReturn(Optional.of(matchMock.getTournament()));
        when(matchRepository.existsByMatchPage(request.getMatchPage())).thenReturn(false);
        when(matchRepository.save(any(Match.class))).thenReturn(matchMock);
        when(participantRepository.findByNameAndDiscipline(request.getParticipant1(), matchMock.getTournament().getDiscipline()))
                .thenReturn(Optional.of(matchMock.getParticipant1()));
        when(participantRepository.findByNameAndDiscipline(request.getParticipant2(), matchMock.getTournament().getDiscipline()))
                .thenReturn(Optional.of(matchMock.getParticipant2()));

        doThrow(new RuntimeException("Market initialization failed")).when(oddsClient).initializeMarkets(any(MarketInitializationRequest.class));

        assertThrows(AppException.class, () -> matchService.addMatch(request));

        verify(matchRepository, times(1)).delete(any(Match.class));
    }

    @Test
    @DisplayName("Get tournaments and matches")
    void getTournamentsAndMatches() {
        Match matchMock = MatchMocks.getMatchMock(1L);
        matchMock.setMatchMaps(List.of(
                new MatchMap(matchMock.getId(), 1, "Map1", 8, 3, 0.4, 0.6, 0.7, 0.3, null, null, matchMock),
                new MatchMap(matchMock.getId(), 2, "Map2", 0, 0, 0.4, 0.6, 0.4, 0.6, null, null, matchMock),
                new MatchMap(matchMock.getId(), 3, "Map3", 0, 0, 0.4, 0.6, 0.4, 0.6, null, null, matchMock)
        ));

        matchMock.setMarkets(MarketMocks.getMarketPairMocks(matchMock, "WinnerMatch", 1));

        when(redisService.getTournaments()).thenReturn(null);
        when(matchRepository.findByStartDateAfterAndStatusNot(any(), any())).thenReturn(List.of(matchMock));


        List<TournamentDto> response = matchService.getTournamentsAndMatches();

        verify(redisService, times(1)).cacheTournaments(response);

        assertEquals(1, response.size());
        TournamentDto dto = response.getFirst();

        assertEquals(1, response.size());
        assertEquals(matchMock.getTournament().getId(), dto.getId());
        assertEquals(matchMock.getTournament().getImage(), dto.getImage());
        assertEquals(matchMock.getTournament().getTitle(), dto.getTitle());
        assertEquals(matchMock.getTournament().getDiscipline(), dto.getDiscipline());

        assertEquals(1, dto.getMatches().size());

        MatchDto matchDto = dto.getMatches().getFirst();
        assertEquals(matchMock.getId(), matchDto.getId());
        assertEquals(matchMock.getParticipant1().getName(), matchDto.getParticipant1().getName());
        assertEquals(matchMock.getParticipant2().getName(), matchDto.getParticipant2().getName());
        assertEquals(matchMock.getStartDate().toEpochSecond(ZoneOffset.UTC), matchDto.getStartDate());
        assertEquals(1, matchDto.getMatchMaps().size());
        assertEquals(2, matchDto.getWinnerMatchMarkets().getMarkets().size());

    }

    @Test
    @DisplayName("Get match markets")
    void getMatchMarkets() {
        Match matchMock = MatchMocks.getMatchMock(1L);

        List<Market> markets = new ArrayList<>();
        markets.addAll(MarketMocks.getMarketPairMocks(matchMock, "WinnerMatch", 3));
        markets.addAll(MarketMocks.getMarketPairMocks(matchMock, "WinnerMap", 3));
        markets.addAll(MarketMocks.getMarketPairMocks(matchMock, "TotalMaps", 3));
        markets.addAll(MarketMocks.getMarketPairMocks(matchMock, "TotalMapRounds", 3));
        markets.addAll(MarketMocks.getMarketPairMocks(matchMock, "HandicapMaps", 3));
        matchMock.setMarkets(markets);

        when(matchRepository.findById(matchMock.getId())).thenReturn(Optional.of(matchMock));

        List<MarketsByCategory> marketsByCategories = matchService.getMatchMarkets(matchMock.getId());
        Set<String> categories = Set.of("Match Winner", "Winner. Map 1", "Winner. Map 2",
                "Total Maps", "Total. Map 1", "Total. Map 2", "Total. Map 3", "Handicap Maps");

        assertEquals(categories.size(), marketsByCategories.size());

        for (MarketsByCategory marketsByCategory : marketsByCategories) {
            assertTrue(categories.contains(marketsByCategory.getCategory()));
        }

    }

    @Test
    @DisplayName("Handle Match update event")
    void handleMatchUpdateEvent() {
        Match match = MatchMocks.getMatchMock(1L);
        MatchUpdateEvent event = new MatchUpdateEvent(match.getId(), true, false);

        when(matchRepository.findById(match.getId())).thenReturn(Optional.of(match));

        matchService.handleMatchUpdateEvent(event);

        verify(redisService, times(1)).publishUpdatedMatchEvent(eq(match.getId()), eq(null), anyList(), eq(false));

        event = new MatchUpdateEvent(match.getId(), false, true);
        matchService.handleMatchUpdateEvent(event);

        verify(redisService, times(1)).removeMatchFromCache(match.getId());
        verify(redisService, times(1)).publishUpdatedMatchEvent(match.getId(), null, null, true);
    }
}
