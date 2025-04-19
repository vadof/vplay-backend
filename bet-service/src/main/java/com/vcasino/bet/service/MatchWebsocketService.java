package com.vcasino.bet.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vcasino.bet.dto.ws.MatchMarketsUpdateDto;
import com.vcasino.bet.dto.ws.MatchUpdateDto;
import com.vcasino.commonredis.enums.Channel;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
@Slf4j
public class MatchWebsocketService implements MessageListener {

    private final ObjectMapper objectMapper;
    private final SimpMessagingTemplate messagingTemplate;

    @Override
    public void onMessage(@NonNull Message message, byte[] pattern) {
        try {
            String data = new String(message.getBody());
            String channel = new String(message.getChannel());

            if (channel.equals(Channel.BET_MATCH.getName())) {
                MatchUpdateDto matchUpdateDto = objectMapper.readValue(data, MatchUpdateDto.class);
                sendUpdatedMatch(matchUpdateDto);
            } else if (channel.equals(Channel.BET_MATCH_MARKETS.getName())) {
                MatchMarketsUpdateDto matchMarketsUpdateDto = objectMapper.readValue(data, MatchMarketsUpdateDto.class);
                sendUpdatedMatchMarkets(matchMarketsUpdateDto);
            }

        } catch (Exception e) {
            log.error("Error processing message from Redis", e);
        }
    }

    private void sendUpdatedMatch(MatchUpdateDto matchUpdateDto) {
        System.out.println("Send updated match " + matchUpdateDto);
        messagingTemplate.convertAndSend("/topic/matches", matchUpdateDto);
    }

    private void sendUpdatedMatchMarkets(MatchMarketsUpdateDto matchMarketsUpdate) {
        String destination = "/topic/matches/" + matchMarketsUpdate.getMatchId();
        System.out.println("Send updated markets " + matchMarketsUpdate.getMarkets());
        messagingTemplate.convertAndSend(destination, matchMarketsUpdate.getMarkets());
    }

}
