package com.vcasino.clicker.dto.admin;

import com.vcasino.clicker.exception.AppException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;

@Slf4j
public class AbstractAdminController {

    protected void validateAdminRole(String role) {
        if (!role.equals("ROLE_ADMIN")) {
            log.warn("Unauthorized access attempt");
            throw new AppException("", HttpStatus.FORBIDDEN);
        }
    }

}
