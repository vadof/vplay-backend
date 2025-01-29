package com.vcasino.user.controller.common;

import com.vcasino.user.entity.User;
import com.vcasino.user.exception.AppException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

@Slf4j
public abstract class GenericController {
    protected User getCurrentUserAsEntity() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            log.error("No user currently logged while executing this operation");
            throw new AppException(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return (User) authentication.getPrincipal();
    }
}
