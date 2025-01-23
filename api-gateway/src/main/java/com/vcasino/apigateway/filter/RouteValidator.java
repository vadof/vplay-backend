package com.vcasino.apigateway.filter;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.function.Predicate;

@Component
public class RouteValidator {
    public static final List<String> openApiEndpoints = List.of(
            "/api/v1/users/auth/login",
            "/api/v1/users/auth/register",
            "/api/v1/users/auth/refreshToken",
            "/api/v1/users/auth/confirmation",
            "/eureka"
    );

    public Predicate<String> isSecured =
            path -> openApiEndpoints.stream()
                    .noneMatch(path::equals);
}
