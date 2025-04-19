package com.vcasino.apigateway.config;

import com.vcasino.apigateway.filter.GenericAuthFilter;
import com.vcasino.apigateway.filter.RouteValidator;
import com.vcasino.apigateway.util.JwtUtil;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BeanConfig {

    @Bean
    public GatewayFilter userServiceAuthFilter(RouteValidator validator, JwtUtil jwtUtil) {
        return new GenericAuthFilter(jwtUtil, validator.isSecuredUserService);
    }

    @Bean
    public GatewayFilter betServiceAuthFilter(RouteValidator validator, JwtUtil jwtUtil) {
        return new GenericAuthFilter(jwtUtil, validator.isSecuredBetService);
    }

}
