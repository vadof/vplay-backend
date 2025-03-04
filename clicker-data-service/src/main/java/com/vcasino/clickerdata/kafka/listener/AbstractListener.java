package com.vcasino.clickerdata.kafka.listener;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@AllArgsConstructor
public abstract class AbstractListener {

    protected final Logger log = LoggerFactory.getLogger(this.getClass());
    private final ObjectMapper mapper;

    protected <T> T fromJson(String jsonString, Class<T> clazz) {
        try {
            return mapper.readValue(jsonString, clazz);
        } catch (JsonProcessingException e) {
            log.error("Error converting {} to {}", jsonString, clazz);
            throw new RuntimeException(e);
        }
    }

}
