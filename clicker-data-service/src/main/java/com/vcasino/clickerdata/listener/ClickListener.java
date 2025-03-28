package com.vcasino.clickerdata.listener;

import com.vcasino.clickerdata.service.ClickService;
import com.vcasino.commonkafka.event.ClickEvent;
import lombok.AllArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@AllArgsConstructor
public class ClickListener {

    private final ClickService clickService;

    @KafkaListener(
            groupId = "clicker-data-service-group",
            topics = "click-events",
            containerFactory = "kafkaListenerBatchFactory"
    )
    public void handle(List<ClickEvent> clickEvents) {
        clickService.handleClicks(clickEvents);
    }
}

