package com.vcasino.apigateway.filter;

import com.vcasino.apigateway.util.JwtUtil;
import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.ArrayList;

@Component
@Slf4j
public class AuthFilter implements GatewayFilter {

    private final JwtUtil jwtUtil;

    @Autowired
    public AuthFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        URI uri = request.getURI();
        log.info("{} request to {}", request.getMethod(), uri);

        String path = uri.getPath();

        if (!request.getHeaders().containsKey(HttpHeaders.AUTHORIZATION)) {
            return onError(exchange, HttpStatus.UNAUTHORIZED);
        }

        String token = request.getHeaders().get(HttpHeaders.AUTHORIZATION).get(0);
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }

        try {
            if (jwtUtil.isTokenExpired(token)) {
                return onError(exchange, HttpStatus.UNAUTHORIZED);
            }

            Claims claims = jwtUtil.extractAllClaims(token);

            if (path.contains("admin") && !hasAdminRole(claims)) {
                return onError(exchange, HttpStatus.FORBIDDEN);
            }

            request = request.mutate()
                    .header("loggedInUser", getUserId(claims))
                    .header("userRole", getRole(claims))
                    .build();
        } catch (Exception e) {
            return onError(exchange, HttpStatus.UNAUTHORIZED);
        }

        return chain.filter(exchange.mutate().request(request).build());
    }

    private Mono<Void> onError(ServerWebExchange exchange, HttpStatus httpStatus) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(httpStatus);
        return response.setComplete();
    }

    private boolean hasAdminRole(Claims claims) {
        return claims.get("roles", ArrayList.class)
                .contains("ROLE_ADMIN");
    }

    private String getRole(Claims claims) {
        return (String) claims.get("roles", ArrayList.class).get(0);
    }

    private String getUserId(Claims claims) {
        return String.valueOf(claims.get("id"));
    }
}