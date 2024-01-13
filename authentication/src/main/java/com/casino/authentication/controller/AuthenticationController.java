package com.casino.authentication.controller;


import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.casino.authentication.dto.AuthenticationRequest;
import com.casino.authentication.dto.AuthenticationResponse;
import com.casino.authentication.dto.UserDto;
import com.casino.authentication.service.AuthenticationService;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/api/v1/auth")
@AllArgsConstructor
@Validated
@Slf4j
public class AuthenticationController {

    private final AuthenticationService authenticationService;

    @PostMapping("/register")
    public ResponseEntity<AuthenticationResponse> register(@RequestBody @Valid UserDto userDto) {
        log.info("REST request to register User");
        AuthenticationResponse res = authenticationService.register(userDto);
        return ResponseEntity.ok().body(res);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthenticationResponse> authenticate(@RequestBody AuthenticationRequest request) {
        log.info("REST request to login {}", request.getUsername());
        return ResponseEntity.ok().body(authenticationService.authenticate(request));
    }

}