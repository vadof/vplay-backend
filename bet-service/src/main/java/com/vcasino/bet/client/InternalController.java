package com.vcasino.bet.client;

import com.vcasino.bet.service.TransactionService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/internal")
@AllArgsConstructor
@Validated
@Slf4j
public class InternalController {

    private final TransactionService transactionService;

    @PostMapping("/events/status")
    public ResponseEntity<EventStatusResponse> getEventStatuses(@Valid @RequestBody EventStatusRequest request) {
        log.debug("Request to get {} event statuses", request.getEventIds().size());
        EventStatusResponse response = transactionService.getEventStatuses(request);
        return ResponseEntity.ok(response);
    }

}
