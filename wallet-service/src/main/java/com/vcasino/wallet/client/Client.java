package com.vcasino.wallet.client;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

public interface Client {
    @PostMapping("/internal/events/status")
    ResponseEntity<EventStatusResponse> getEventStatuses(@RequestBody EventStatusRequest request);
}
