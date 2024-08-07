package com.vcasino.clicker.controller;

import com.vcasino.clicker.dto.admin.AbstractAdminController;
import com.vcasino.clicker.dto.admin.AccountImprove;
import com.vcasino.clicker.entity.Account;
import com.vcasino.clicker.service.admin.AdminService;
import io.swagger.v3.oas.annotations.Hidden;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Hidden
@RestController
@RequestMapping("/api/v1/clicker/admin")
@AllArgsConstructor
@Validated
@Slf4j
public class AdminController extends AbstractAdminController {

    private final AdminService adminService;

    @PostMapping(value = "/improve",
            consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Account> improveAccount(@RequestBody @Valid AccountImprove accountImprove,
                                                  @RequestHeader("userRole") String role) {
        log.info("REST request to improve Account#{}", accountImprove.getAccountId());
        validateAdminRole(role);
        Account account = adminService.improveAccount(accountImprove);
        return ResponseEntity.ok().body(account);
    }


}
