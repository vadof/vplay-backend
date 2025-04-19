package com.vcasino.apigateway.filter;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

@Component
public class RouteValidator {
    public static final Set<String> openUserServiceEndpoints = Set.of(
            "/api/v1/users/auth/login",
            "/api/v1/users/auth/register",
            "/api/v1/users/auth/refreshToken",
            "/api/v1/users/auth/email-confirmation",
            "/api/v1/users/auth/email-confirmation-resend",
            "/api/v1/users/auth/username-confirmation",
            "/api/v1/users/auth/delete-pending-user",
            "/eureka"
    );

    public static final List<String> openBetServiceEndpoints = List.of(
            "/api/v1/bet/matches",
            "/api/v1/bet/images",
            "/api/v1/bet/ws"
    );

    public Predicate<String> isSecuredUserService = path -> !openUserServiceEndpoints.contains(path);
    public Predicate<String> isSecuredBetService = path -> openBetServiceEndpoints.stream()
            .noneMatch(path::startsWith);
}
