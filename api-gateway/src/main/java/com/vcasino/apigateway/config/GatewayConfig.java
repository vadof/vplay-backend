package com.vcasino.apigateway.config;

import com.google.common.net.HttpHeaders;
import com.vcasino.apigateway.filter.AdminFilter;
import com.vcasino.apigateway.filter.AuthFilter;
import lombok.AllArgsConstructor;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@AllArgsConstructor
public class GatewayConfig {

    private final GatewayFilter userServiceAuthFilter;
    private final GatewayFilter betServiceAuthFilter;
    private final AuthFilter authFilter;
    private final AdminFilter adminFilter;
    private final GatewayFilter notificationsFilter;
    private final ApplicationConfig applicationConfig;

    @Bean
    public RouteLocator routeLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                .route("user-service", r -> r.path("/api/*/users/**")
                        .filters(f -> f.filter(userServiceAuthFilter)
                                .circuitBreaker(config -> config
                                        .setName("user-service")
                                        .setFallbackUri("forward:/fallback")))
                        .uri("lb://user-service"))
                .route("clicker-service", r -> r.path("/api/*/clicker/**")
                        .filters(f -> f.filter(authFilter)
                                .circuitBreaker(config -> config
                                        .setName("clicker-service")
                                        .setFallbackUri("forward:/fallback")))
                        .uri("lb://clicker-service"))
                .route("clicker-data", r -> r.path("/api/*/clicker-data/**")
                        .filters(f -> f.filter(adminFilter))
                        .uri("lb://clicker-data-service"))
                .route("wallet-service", r -> r.path("/api/*/wallet/**")
                        .filters(f -> f.filter(authFilter)
                                .circuitBreaker(config -> config
                                        .setName("wallet-service")
                                        .setFallbackUri("forward:/fallback")))
                        .uri("lb://wallet-service"))
                .route("notification-service", r -> r.path("/api/*/notifications/**")
                        .filters(f -> f.filter(notificationsFilter)
                                .circuitBreaker(config -> config
                                        .setName("notification-service")
                                        .setFallbackUri("forward:/fallback")))
                        .uri("lb://notification-service"))
                .route("bet-service", r -> r.path("/api/*/bet/**")
                        .filters(f -> f.filter(betServiceAuthFilter)
                                .circuitBreaker(config -> config
                                        .setName("bet-service")
                                        .setFallbackUri("forward:/fallback")))
                        .uri("lb://bet-service"))
                .build();
    }

    @Bean
    public CorsWebFilter corsWebFilter() {
        CorsConfiguration corsConfiguration = new CorsConfiguration();
        corsConfiguration.setAllowCredentials(true);
        corsConfiguration.setAllowedHeaders(List.of(HttpHeaders.CONTENT_TYPE, HttpHeaders.AUTHORIZATION, HttpHeaders.ORIGIN, HttpHeaders.ACCEPT, HttpHeaders.UPGRADE, HttpHeaders.CONNECTION));
        corsConfiguration.setAllowedMethods(List.of("GET", "POST", "PATCH", "PUT", "DELETE"));
        corsConfiguration.addAllowedOrigin(applicationConfig.getClientUrl());
        corsConfiguration.addAllowedOrigin(applicationConfig.getAdminClientUrl());
        corsConfiguration.addExposedHeader(HttpHeaders.SET_COOKIE);
        UrlBasedCorsConfigurationSource corsConfigurationSource = new UrlBasedCorsConfigurationSource();
        corsConfigurationSource.registerCorsConfiguration("/**", corsConfiguration);
        return new CorsWebFilter(corsConfigurationSource);
    }

}
