package com.casino.finance.controller;

import com.casino.finance.dto.ConversionDto;
import com.casino.finance.service.FinanceService;
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
@RequestMapping("/api/v1/finances")
@AllArgsConstructor
@Validated
@Slf4j
public class FinanceController {

    private final FinanceService financeService;

    @PostMapping("/convert")
    public ResponseEntity<ConversionDto> convertMoney(@RequestBody @Valid ConversionDto conversionDto) {
        log.info("REST request to convert money {}", conversionDto);
        ConversionDto updated = financeService.convertMoney(conversionDto);
        return ResponseEntity.ok().body(updated);
    }
}
