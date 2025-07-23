package com.vcasino.clicker.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vcasino.clicker.config.ApplicationConfig;
import com.vcasino.clicker.config.IntegratedService;
import com.vcasino.clicker.dto.youtube.VideoInfo;
import com.vcasino.clicker.exception.AppException;
import com.vcasino.clicker.service.video.YoutubeService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestTemplate;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalTime;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * UNIT tests for {@link YoutubeService}
 */
@ExtendWith(MockitoExtension.class)
public class VideoServiceTest {

    @Mock
    RestTemplate restTemplate;

    @Mock
    RedisService redisService;

    @Mock
    ApplicationConfig appConfig;

    @Mock
    ObjectMapper objectMapper;

    @InjectMocks
    YoutubeService youtubeService;

    @Test
    @DisplayName(value = "Get integrated service")
    void getIntegratedService() {
        assertEquals(IntegratedService.YOUTUBE, youtubeService.getIntegratedService());
    }

    @Test
    @DisplayName(value = "Get video info")
    void getVideoInfo() throws Exception {
        String id = "id";
        long seconds = 23L;
        String duration = "PT%sS".formatted(seconds);

        VideoInfo videoInfo = mockVideoResponse(id, duration, 1);
        assertEquals(id, videoInfo.getId());
        assertEquals(LocalTime.ofSecondOfDay(seconds), videoInfo.getDuration());
        verify(redisService, times(1)).save(eq("youtube_video:" + videoInfo.getId()), eq(videoInfo), anyLong());
    }

    @Test
    @DisplayName(value = "Get video test duration")
    void getVideoInfoDuration() throws Exception {
        String id = "id";
        long seconds = 23L;
        int minutes = 1;
        String duration = "PT%sM%sS".formatted(minutes, seconds);

        VideoInfo videoInfo = mockVideoResponse(id, duration, 1);
        assertEquals(id, videoInfo.getId());
        assertEquals(LocalTime.ofSecondOfDay(seconds + minutes * 60), videoInfo.getDuration());
    }

    @Test
    @DisplayName(value = "Get video not found")
    void getVideoInfoNotFound() {
        String id = "id";
        long seconds = 23L;
        int minutes = 1;
        String duration = "PT%sM%sS".formatted(minutes, seconds);

        assertThrows(AppException.class, () -> mockVideoResponse(id, duration, 0));
    }

    @Test
    @DisplayName(value = "Get video found more than 1 video")
    void getVideoInfoFoundMoreThan1() {
        String id = "id";
        long seconds = 23L;
        int minutes = 1;
        String duration = "PT%sM%sS".formatted(minutes, seconds);

        assertThrows(AppException.class, () -> mockVideoResponse(id, duration, 2));
    }

    @Test
    @DisplayName(value = "Get video too long")
    void getVideoInfoTooLong() {
        String id = "id";
        int hours = 25;
        String duration = "PT%sH".formatted(hours);

        assertThrows(AppException.class, () -> mockVideoResponse(id, duration, 1));
    }

    private VideoInfo mockVideoResponse(String id, String duration, Integer totalResults) throws Exception {
        ClassLoader classLoader = this.getClass().getClassLoader();
        String pathStr = this.getClass().getSimpleName() + "/youtube-response.json";
        Path path = Paths.get(Objects.requireNonNull(classLoader.getResource(pathStr)).toURI());
        String videoResponse = Files.readString(path)
                .replace("${id}", id)
                .replace("${duration}", duration)
                .replace("${totalResults}", totalResults.toString());

        when(restTemplate.getForObject(anyString(), eq(String.class))).thenReturn(videoResponse);
        when(objectMapper.readTree(videoResponse)).thenReturn(new ObjectMapper().readTree(videoResponse));

        return youtubeService.getVideoInfo(id);
    }

}
