package com.vcasino.wallet.controller;

import com.vcasino.wallet.exception.AppException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;

@Slf4j
@AllArgsConstructor
public abstract class GenericController {

    private final HttpServletRequest request;

    protected void validateAdminRole() {
        String role = getRole();
        if (!role.equals("ROLE_ADMIN")) {
            log.warn("Unauthorized access attempt");
            throw new AppException(null, HttpStatus.FORBIDDEN);
        }
    }

    protected Long getLoggedInUserId() {
        String id = request.getHeader(CustomHeader.LOGGED_IN_USER.getValue());
        if (id == null) {
            log.warn("User ID header not found");
            throw new AppException("User ID header not found", HttpStatus.UNAUTHORIZED);
        }
        try {
            return Long.parseLong(id);
        } catch (NumberFormatException e) {
            log.error("Invalid user ID format: {}", id);
            throw new AppException("Invalid user ID format", HttpStatus.BAD_REQUEST);
        }
    }

    protected String getRole() {
        return request.getHeader(CustomHeader.ROLE.getValue());
    }

}
