package com.vcasino.clicker.service.video;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vcasino.clicker.config.ApiKeys;
import com.vcasino.clicker.config.IntegratedService;
import com.vcasino.clicker.dto.youtube.VideoInfo;
import com.vcasino.clicker.exception.AppException;
import com.vcasino.clicker.service.RedisService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.time.DateTimeException;
import java.time.Duration;
import java.time.LocalTime;

@Service
@Slf4j
public class YoutubeService extends VideoService {

    private final RedisService redisService;
    private final static String VIDEO_INFO_CACHE_KEY = "youtube_video:";

    public YoutubeService(RestTemplate restTemplate, ApiKeys apiKeys, ObjectMapper objectMapper, RedisService redisService) {
        super(restTemplate, apiKeys, objectMapper);
        this.redisService = redisService;
    }

    public VideoInfo getVideoInfo(String videoId) {
        VideoInfo cachedVideoInfo = redisService.get(VIDEO_INFO_CACHE_KEY + videoId, VideoInfo.class);
        if (cachedVideoInfo != null) return cachedVideoInfo;

        String url = "https://www.googleapis.com/youtube/v3/videos?id=%s&part=contentDetails&key=%s"
                .formatted(videoId, apiKeys.getYoutubeApiKey());

        String jsonResponse = restTemplate.getForObject(url, String.class);

        try {
            JsonNode root = objectMapper.readTree(jsonResponse);

            int totalResults = root.path("pageInfo").path("totalResults").asInt();
            if (totalResults == 0) {
                throw new AppException("Video not found.", HttpStatus.BAD_REQUEST);
            } else if (totalResults > 1) {
                throw new AppException("Found " + totalResults + " results. Please type correct id.", HttpStatus.BAD_REQUEST);
            }

            JsonNode items = root.path("items");
            JsonNode item = items.get(0);

            String id = item.path("id").asText();
            String durationString = item.path("contentDetails").path("duration").asText();

            long seconds = Duration.parse(durationString).getSeconds();
            LocalTime duration = LocalTime.ofSecondOfDay(seconds);

            VideoInfo videoInfo = new VideoInfo(id, duration);
            redisService.save(VIDEO_INFO_CACHE_KEY + id, videoInfo, 15);
            return videoInfo;
        } catch (IOException e) {
            throw new AppException("An error occurred while processing the video response.",
                    HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (DateTimeException e) {
            throw new AppException("Video is too long", HttpStatus.BAD_REQUEST);
        }
    }

    public IntegratedService getIntegratedService() {
        return IntegratedService.YOUTUBE;
    }
}
