package com.vcasino.clicker.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@AllArgsConstructor
@Slf4j
public class RedisService {
    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    public <T> void save(String key, T data, long ttlMinutes) {
        try {
            String jsonData = objectMapper.writeValueAsString(data);
            redisTemplate.opsForValue().set(key, jsonData, ttlMinutes, TimeUnit.MINUTES);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize object for Redis", e);
        }
    }

    public <T> void save(String key, T data) {
        try {
            String jsonData = objectMapper.writeValueAsString(data);
            redisTemplate.opsForValue().set(key, jsonData);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize object for Redis", e);
        }
    }

    public <T> T get(String key, Class<T> clazz) {
        String jsonData = redisTemplate.opsForValue().get(key);
        if (jsonData == null) return null;

        try {
            return objectMapper.readValue(jsonData, clazz);
        } catch (JsonProcessingException e) {
            log.error("Failed to deserialize object from Redis", e);
            return null;
        }
    }

    public <T> T get(String key, TypeReference<T> typeReference) {
        String jsonData = redisTemplate.opsForValue().get(key);
        if (jsonData == null) return null;

        try {
            return objectMapper.readValue(jsonData, typeReference);
        } catch (JsonProcessingException e) {
            log.error("Failed to deserialize object from Redis", e);
            return null;
        }
    }
}
