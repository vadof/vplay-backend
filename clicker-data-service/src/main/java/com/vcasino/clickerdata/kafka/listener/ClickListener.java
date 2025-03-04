package com.vcasino.clickerdata.kafka.listener;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vcasino.clickerdata.kafka.message.ClickEvent;
import com.vcasino.clickerdata.service.ClickService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ClickListener extends AbstractListener {

    private final ClickService clickService;

    public ClickListener(ObjectMapper mapper, ClickService clickService) {
        super(mapper);
        this.clickService = clickService;
    }

    @KafkaListener(
            groupId = "clicker-data-service-group",
            topics = "click-events",
            containerFactory = "batchFactory"
    )
    public void handle(List<String> data) {
        List<ClickEvent> clickEvents = data.stream()
                .map(s -> fromJson(s, ClickEvent.class))
                .toList();

        clickService.handleClicks(clickEvents);
    }
}

