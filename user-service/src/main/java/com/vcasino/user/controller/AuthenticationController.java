package com.vcasino.user.controller;


import com.vcasino.user.dto.AuthenticationRequest;
import com.vcasino.user.dto.AuthenticationResponse;
import com.vcasino.user.dto.TokenRefreshRequest;
import com.vcasino.user.dto.TokenRefreshResponse;
import com.vcasino.user.dto.UserDto;
import com.vcasino.user.service.AuthenticationService;
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
@RequestMapping("/api/v1/users/auth")
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
    public ResponseEntity<AuthenticationResponse> authenticate(@RequestBody @Valid AuthenticationRequest request) {
        log.info("REST request to login {}", request.getUsername());
        return ResponseEntity.ok().body(authenticationService.authenticate(request));
    }

    @PostMapping("/refreshToken")
    public ResponseEntity<TokenRefreshResponse> refreshToken(@RequestBody @Valid TokenRefreshRequest request) {
        log.info("REST request to refresh token {}", request.getRefreshToken());
        TokenRefreshResponse response = authenticationService.refreshToken(request);
        return ResponseEntity.ok().body(response);
    }
}