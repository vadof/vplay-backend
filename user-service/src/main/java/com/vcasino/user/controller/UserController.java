package com.vcasino.user.controller;

import com.vcasino.user.controller.common.GenericController;
import com.vcasino.user.dto.UserDto;
import com.vcasino.user.service.UserService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users")
@AllArgsConstructor
@Slf4j
public class UserController extends GenericController {

    private final UserService userService;

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<UserDto> getUser() {
        log.info("REST request to get User");
        UserDto user = userService.getUser(getCurrentUserAsEntity());
        return ResponseEntity.ok().body(user);
    }

}
