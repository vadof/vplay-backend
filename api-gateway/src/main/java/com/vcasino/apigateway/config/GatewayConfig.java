package com.vcasino.apigateway.config;

import com.google.common.net.HttpHeaders;
import com.vcasino.apigateway.filter.AdminFilter;
import com.vcasino.apigateway.filter.AuthFilter;
import lombok.AllArgsConstructor;
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

    private final AuthFilter authFilter;
    private final AdminFilter adminFilter;
    private final ApplicationConfig applicationConfig;

    @Bean
    public RouteLocator routeLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                .route("user-service", r -> r.path("/api/*/users/**")
                        .filters(f -> f.filter(authFilter))
                        .uri("lb://user-service"))
                .route("clicker-service", r -> r.path("/api/*/clicker/**")
                        .filters(f -> f.filter(authFilter))
                        .uri("lb://clicker-service"))
                .route("clicker-data", r -> r.path("/api/*/clicker-data/**")
                        .filters(f -> f.filter(adminFilter))
                        .uri("lb://clicker-data-service"))
                .route("clicker-service", r -> r.path("/api/*/wallet/**")
                        .filters(f -> f.filter(authFilter))
                        .uri("lb://wallet-service"))
                .build();
    }

    @Bean
    public CorsWebFilter corsWebFilter() {
        CorsConfiguration corsConfiguration = new CorsConfiguration();
        corsConfiguration.setAllowCredentials(true);
        corsConfiguration.setAllowedHeaders(List.of(HttpHeaders.CONTENT_TYPE, HttpHeaders.AUTHORIZATION));
        corsConfiguration.setAllowedMethods(List.of("GET", "POST", "PATCH", "PUT", "DELETE"));
        corsConfiguration.addAllowedOrigin(applicationConfig.getClientUrl());
        corsConfiguration.addAllowedOrigin(applicationConfig.getAdminClientUrl());
        corsConfiguration.addExposedHeader(HttpHeaders.SET_COOKIE);
        UrlBasedCorsConfigurationSource corsConfigurationSource = new UrlBasedCorsConfigurationSource();
        corsConfigurationSource.registerCorsConfiguration("/**", corsConfiguration);
        return new CorsWebFilter(corsConfigurationSource);
    }

}
