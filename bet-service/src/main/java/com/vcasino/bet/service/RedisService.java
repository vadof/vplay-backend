package com.vcasino.bet.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vcasino.bet.dto.response.MarketDto;
import com.vcasino.bet.dto.response.MarketPairDto;
import com.vcasino.bet.dto.response.MatchDto;
import com.vcasino.bet.dto.response.MatchMapDto;
import com.vcasino.bet.dto.RedisTournamentDto;
import com.vcasino.bet.dto.response.TournamentDto;
import com.vcasino.bet.dto.ws.MarketWsDto;
import com.vcasino.bet.dto.ws.MatchMarketsUpdateDto;
import com.vcasino.bet.dto.ws.MatchUpdateDto;
import com.vcasino.bet.entity.market.Market;
import com.vcasino.commonredis.enums.Channel;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
@AllArgsConstructor
@Slf4j
public class RedisService {

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;
    public final String TOURNAMENTS_KEY = "esport-tournaments";
    public final String MATCH_KEY = "esport-matches:";

    public void cacheTournaments(List<TournamentDto> tournaments) {
        try {
            List<RedisTournamentDto> tournamentDtos = new ArrayList<>();

            for (TournamentDto tournament : tournaments) {
                List<Long> matchKeys = new ArrayList<>();
                for (MatchDto match : tournament.getMatches()) {
                    matchKeys.add(match.getId());
                    String value = objectMapper.writeValueAsString(match);
                    redisTemplate.opsForValue().set(MATCH_KEY + match.getId(), value, 30, TimeUnit.MINUTES);
                }

                RedisTournamentDto tournamentDto = new RedisTournamentDto(tournament.getId(), tournament.getImage(),
                        tournament.getTitle(), tournament.getDiscipline(), matchKeys);
                tournamentDtos.add(tournamentDto);
            }

            String value = objectMapper.writeValueAsString(tournamentDtos);
            redisTemplate.opsForValue().set(TOURNAMENTS_KEY, value, 30, TimeUnit.MINUTES);
        } catch (Exception e) {
            log.error("Error caching tournaments", e);
        }
    }

    public List<TournamentDto> getTournaments() {
        try {
            String data = redisTemplate.opsForValue().get(TOURNAMENTS_KEY);
            if (data == null) return null;

            List<RedisTournamentDto> tournaments = objectMapper.readValue(data, new TypeReference<List<RedisTournamentDto>>() {});
            List<TournamentDto> tournamentDtos = new ArrayList<>();
            for (RedisTournamentDto redisTournamentDto : tournaments) {
                List<MatchDto> matches = new ArrayList<>();

                for (Long matchKey : redisTournamentDto.getMatchKeys()) {
                    String matchData = redisTemplate.opsForValue().get(MATCH_KEY + matchKey);
                    if (matchData == null) {
                        redisTemplate.delete(TOURNAMENTS_KEY);
                        return null;
                    }

                    matches.add(objectMapper.readValue(matchData, MatchDto.class));
                }

                tournamentDtos.add(new TournamentDto(redisTournamentDto.getId(), redisTournamentDto.getImage(),
                        redisTournamentDto.getTitle(), redisTournamentDto.getDiscipline(), matches));
            }

            return tournamentDtos;

        } catch (Exception e) {
            log.error("Error getting tournaments from cache", e);
            redisTemplate.delete(TOURNAMENTS_KEY);
            return null;
        }
    }

    public void updateTournamentMatchMarkets(Long matchId, List<Market> markets) {
        String data = redisTemplate.opsForValue().get(MATCH_KEY + matchId);
        if (data == null) return;

        try {
            MatchDto match = objectMapper.readValue(data, MatchDto.class);
            MarketPairDto winnerMatchMarkets = match.getWinnerMatchMarkets();

            Map<Long, Market> marketMap = new HashMap<>();
            for (Market market : markets) {
                marketMap.put(market.getId(), market);
            }

            boolean updated = false;

            for (MarketDto marketDto : winnerMatchMarkets.getMarkets()) {
                Market market = marketMap.get(marketDto.getId());
                if (market != null) {
                    marketDto.setOdds(market.getOdds());
                    marketDto.setClosed(market.getClosed());
                    updated = true;
                }
            }

            winnerMatchMarkets.setClosed(winnerMatchMarkets.getMarkets().getFirst().getClosed());

            if (updated) {
                String value = objectMapper.writeValueAsString(match);
                redisTemplate.opsForValue().set(MATCH_KEY + matchId, value, 30, TimeUnit.MINUTES);
            }
        } catch (Exception e) {
            log.error("Error updating match cache", e);
            redisTemplate.delete(TOURNAMENTS_KEY);
        }
    }

    public void cacheMatchMarkets(List<MarketDto> marketDtos) {
        try {
            String value = objectMapper.writeValueAsString(marketDtos);
            redisTemplate.opsForValue().set(TOURNAMENTS_KEY, value, 15, TimeUnit.MINUTES);
        } catch (Exception e) {
            log.error("Error caching match markets", e);
        }
    }

    public void removeMatchFromCache(Long key) {
        redisTemplate.delete(MATCH_KEY + key);

        String data = redisTemplate.opsForValue().get(TOURNAMENTS_KEY);
        if (data == null) return;

        try {
            List<RedisTournamentDto> tournaments = objectMapper.readValue(data, new TypeReference<List<RedisTournamentDto>>() {});
            for (RedisTournamentDto tournament : tournaments) {
                if (tournament.getMatchKeys().remove(key)) {
                    if (tournament.getMatchKeys().isEmpty()) {
                        redisTemplate.delete(TOURNAMENTS_KEY);
                    } else {
                        String value = objectMapper.writeValueAsString(tournaments);
                        redisTemplate.opsForValue().set(TOURNAMENTS_KEY, value, 30, TimeUnit.MINUTES);
                    }
                    break;
                }
            }

        } catch (Exception e) {
            log.error("Failed to recache tournaments", e);
        }
    }

    public void publishUpdatedMatchEvent(Long matchId, List<Market> markets, List<MatchMapDto> matchMaps, boolean ended) {
        MatchUpdateDto matchUpdateDto = new MatchUpdateDto(matchId, toDto(markets), matchMaps, ended);
        try {
            String data = objectMapper.writeValueAsString(matchUpdateDto);
            redisTemplate.convertAndSend(Channel.BET_MATCH.getName(), data);
        } catch (JsonProcessingException e) {
            log.error("Error converting object to string {}", matchUpdateDto, e);
        }
    }

    public void publishUpdatedMarketsEvent(Long matchId, List<Market> markets) {
        MatchMarketsUpdateDto matchUpdateDto = new MatchMarketsUpdateDto(matchId, toDto(markets));
        try {
            String data = objectMapper.writeValueAsString(matchUpdateDto);
            redisTemplate.convertAndSend(Channel.BET_MATCH_MARKETS.getName(), data);
        } catch (JsonProcessingException e) {
            log.error("Error converting object to string {}", matchUpdateDto, e);
        }
    }

    private List<MarketWsDto> toDto(List<Market> markets) {
        if (markets == null || markets.isEmpty()) return null;
        return markets.stream()
                .map(m -> new MarketWsDto(m.getId(), m.getClosed(), m.getOdds())).toList();
    }

}
