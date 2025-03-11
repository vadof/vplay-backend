package com.vcasino.clicker.controller.admin;

import com.vcasino.clicker.controller.common.GenericController;
import com.vcasino.clicker.dto.AccountDto;
import com.vcasino.clicker.dto.admin.AccountImprove;
import com.vcasino.clicker.dto.admin.FrozenStatus;
import com.vcasino.clicker.service.admin.AdminService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/clicker/admin/accounts")
@Validated
@Slf4j
public class AdminAccountController extends GenericController {
    private final AdminService adminService;

    public AdminAccountController(HttpServletRequest request, AdminService adminService) {
        super(request);
        this.adminService = adminService;
    }

    @PostMapping(value = "/improve",
            consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<AccountDto> improveAccount(@RequestBody @Valid AccountImprove accountImprove) {
        log.info("REST request to improve Account#{}", accountImprove.getAccountId());
        validateAdminRole();
        AccountDto account = adminService.improveAccount(accountImprove);
        return ResponseEntity.ok().body(account);
    }

    @PostMapping(value = "/frozen-status",
            consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> changeFrozenStatus(@RequestBody @Valid FrozenStatus frozenStatus) {
        log.info("REST request to change frozen status to {} for Account#{}", frozenStatus.getStatus(), frozenStatus.getAccountId());
        validateAdminRole();
        adminService.changeFrozenStatus(frozenStatus);
        return ResponseEntity.accepted().body(null);
    }
}
