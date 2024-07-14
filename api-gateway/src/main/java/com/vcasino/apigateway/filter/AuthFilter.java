package com.vcasino.apigateway.filter;

import com.vcasino.apigateway.util.JwtUtil;
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

@Component
public class AuthFilter implements GatewayFilter {

    private final RouteValidator routeValidator;
    private final JwtUtil jwtUtil;

    @Autowired
    public AuthFilter(RouteValidator routeValidator, JwtUtil jwtUtil) {
        this.routeValidator = routeValidator;
        this.jwtUtil = jwtUtil;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        System.out.println("HERE");
        ServerHttpRequest request = null;
        if (routeValidator.isSecured.test(exchange.getRequest())) {
            System.out.println("HERE 2");
            if (!exchange.getRequest().getHeaders().containsKey(HttpHeaders.AUTHORIZATION)) {
                return onError(exchange, HttpStatus.UNAUTHORIZED);
            }

            String token = exchange.getRequest().getHeaders().get(HttpHeaders.AUTHORIZATION).get(0);
            if (token != null && token.startsWith("Bearer ")) {
                token = token.substring(7);
            }

            try {
                jwtUtil.validateToken(token);

                request = exchange.getRequest()
                        .mutate()
                        .header("loggedInUser", jwtUtil.extractUsername(token))
                        .build();
            } catch (Exception e) {
                return onError(exchange, HttpStatus.UNAUTHORIZED);
            }
        }
        return chain.filter(exchange.mutate().request(request).build());
    }

    private Mono<Void> onError(ServerWebExchange exchange, HttpStatus httpStatus) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(httpStatus);
        return response.setComplete();
    }
}