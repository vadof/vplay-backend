package com.vcasino.user.controller;


import com.vcasino.user.dto.AuthenticationRequest;
import com.vcasino.user.dto.AuthenticationResponse;
import com.vcasino.user.dto.OAuthConfirmation;
import com.vcasino.user.dto.TokenRefreshRequest;
import com.vcasino.user.dto.TokenRefreshResponse;
import com.vcasino.user.dto.UserDto;
import com.vcasino.user.entity.Role;
import com.vcasino.user.service.AuthenticationService;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@Tag(name = "Authentication", description = "API operations with Authentication")
@RestController
@RequestMapping("/api/v1/users/auth")
@AllArgsConstructor
@Validated
@Slf4j
public class AuthenticationController {

    private final AuthenticationService authenticationService;

    @Operation(summary = "Register new account")
    @ApiResponse(responseCode = "200", description = "Account created",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = AuthenticationResponse.class)))
    @PostMapping(value = "/register", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<AuthenticationResponse> register(@RequestBody @Valid UserDto userDto) {
        log.info("REST request to register User");
        AuthenticationResponse response = authenticationService.register(userDto, Role.USER);
        return ResponseEntity.ok().body(response);
    }

    @Hidden
    @PostMapping(value = "/admin/register", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<AuthenticationResponse> registerAdmin(@RequestBody @Valid UserDto userDto) {
        log.info("REST request to register Admin");
        AuthenticationResponse response = authenticationService.register(userDto, Role.ADMIN);
        return ResponseEntity.ok().body(response);
    }

    @Operation(summary = "Login to account")
    @ApiResponse(responseCode = "200", description = "Login successful",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = AuthenticationResponse.class)))
    @PostMapping(value = "/login", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<AuthenticationResponse> authenticate(@RequestBody @Valid AuthenticationRequest request) {
        log.info("REST request to login {}", request.getUsername());
        AuthenticationResponse response = authenticationService.authenticate(request);
        return ResponseEntity.ok().body(response);
    }

    @Operation(summary = "Refresh token")
    @ApiResponse(responseCode = "200", description = "Token refreshed",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = TokenRefreshResponse.class)))
    @PostMapping(value = "/refreshToken", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<TokenRefreshResponse> refreshToken(@RequestBody @Valid TokenRefreshRequest request) {
        log.info("REST request to refresh token {}", request.getRefreshToken());
        TokenRefreshResponse response = authenticationService.refreshToken(request);
        return ResponseEntity.ok().body(response);
    }

    @Operation(summary = "OAuth2 registration confirmation")
    @ApiResponse(responseCode = "200", description = "Account created",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = AuthenticationResponse.class)))
    @PostMapping(value = "/confirmation", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<AuthenticationResponse> oAuthConfirmation(
            @RequestBody @Valid OAuthConfirmation oAuthConfirmation,
            @CookieValue("confirmationToken") String confirmationToken) {
        log.info("REST request to confirm oauth registration");
        AuthenticationResponse response = authenticationService.oAuthConfirmation(oAuthConfirmation.getUsername(), confirmationToken);
        return ResponseEntity.ok().headers(response.getHeaders()).body(response);
    }

}