package com.vcasino.clicker.service.video;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vcasino.clicker.config.ApiKeys;
import com.vcasino.clicker.config.IntegratedService;
import com.vcasino.clicker.dto.youtube.VideoInfo;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@AllArgsConstructor
public abstract class VideoService {

    protected final RestTemplate restTemplate;
    protected final ApiKeys apiKeys;
    protected final ObjectMapper objectMapper;

    public abstract VideoInfo getVideoInfo(String videoId);
    public abstract IntegratedService getIntegratedService();

}
