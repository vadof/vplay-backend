package com.vcasino.user.controller;


import com.vcasino.user.dto.UserDto;
import com.vcasino.user.dto.auth.AuthenticationRequest;
import com.vcasino.user.dto.auth.AuthenticationResponse;
import com.vcasino.user.dto.auth.OAuthConfirmation;
import com.vcasino.user.dto.auth.TokenRefreshRequest;
import com.vcasino.user.dto.auth.TokenRefreshResponse;
import com.vcasino.user.dto.email.EmailConfirmation;
import com.vcasino.user.dto.email.EmailTokenOptionsDto;
import com.vcasino.user.service.AuthenticationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
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
    @ApiResponse(responseCode = "200", description = "An email has been sent to confirm an email. The email can be resent in emailsSent * 30 seconds")
    @PostMapping(value = "/register", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<EmailTokenOptionsDto> register(@RequestBody @Valid UserDto userDto) {
        log.info("REST request to register User");
        EmailTokenOptionsDto tokenOptions = authenticationService.registerUser(userDto);
        return ResponseEntity.ok().body(tokenOptions);
    }

    @Operation(summary = "Register new admin account (admin authentication required)")
    @ApiResponse(responseCode = "201", description = "Account created")
    @PostMapping(value = "/admin/register", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> registerAdmin(@RequestBody @Valid UserDto userDto) {
        log.info("REST request to register Admin");
        authenticationService.registerAdmin(userDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(null);
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
        log.info("REST request to refresh token");
        TokenRefreshResponse response = authenticationService.refreshToken(request);
        return ResponseEntity.ok().body(response);
    }

    @Operation(summary = "After OAuth2 registration user must confirm his username")
    @ApiResponse(responseCode = "200", description = "Account created",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = AuthenticationResponse.class)))
    @PostMapping(value = "/username-confirmation", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<AuthenticationResponse> usernameConfirmation(
            @RequestBody @Valid OAuthConfirmation oAuthConfirmation,
            @CookieValue("confirmationToken") String confirmationToken) {
        log.info("REST request to confirm oauth registration");
        AuthenticationResponse response = authenticationService.confirmUsername(oAuthConfirmation.getUsername(), confirmationToken);
        return ResponseEntity.ok().headers(response.getHeaders()).body(response);
    }

    @Operation(summary = "After registration user must confirm his email")
    @ApiResponse(responseCode = "200", description = "Account created",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = AuthenticationResponse.class)))
    @PostMapping(value = "/email-confirmation", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<AuthenticationResponse> emailConfirmation(@RequestBody @Valid EmailConfirmation emailConfirmation) {
        log.info("REST request to confirm an email");
        AuthenticationResponse response = authenticationService.confirmEmail(emailConfirmation.getConfirmationToken());
        return ResponseEntity.ok().body(response);
    }

    @Operation(summary = "Resend confirmation email. The email can be resent if canResend = true with interval emailsSent * 30 seconds")
    @ApiResponse(responseCode = "200", description = "Email sent",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = EmailTokenOptionsDto.class)))
    @PostMapping(value = "/email-confirmation-resend", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<EmailTokenOptionsDto> resendConfirmationEmail(@RequestBody @Valid EmailTokenOptionsDto tokenOptions) {
        log.info("REST request to resend confirmation email to {}", tokenOptions.getEmail());
        EmailTokenOptionsDto res = authenticationService.resendConfirmationEmail(tokenOptions);
        return ResponseEntity.ok().body(res);
    }

    @Operation(summary = "If a user has registered with an invalid email address, the pending user can be deleted")
    @ApiResponse(responseCode = "200", description = "The pending user has been deleted")
    @PostMapping(value = "/delete-pending-user", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> deletePendingUser(@RequestBody @Valid EmailTokenOptionsDto tokenOptions) {
        log.info("REST request to delete pending user with email {}", tokenOptions.getEmail());
        authenticationService.deletePendingUser(tokenOptions);
        return ResponseEntity.ok().body(null);
    }

}