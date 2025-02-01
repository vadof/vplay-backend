package com.vcasino.user.config.securiy;

import com.vcasino.user.config.ApplicationConfig;
import com.vcasino.user.oauth2.OAuth2LoginFailureHandler;
import com.vcasino.user.oauth2.OAuth2LoginSuccessHandler;
import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.util.ArrayList;
import java.util.List;

import static org.springframework.security.config.http.SessionCreationPolicy.STATELESS;

@Configuration
@EnableWebSecurity
@AllArgsConstructor
public class SecurityConfiguration {

    private final JwtAuthenticationFilter jwtAuthFilter;
    private final AuthenticationProvider authenticationProvider;
    private final OAuth2LoginSuccessHandler oAuth2LoginSuccessHandler;
    private final OAuth2LoginFailureHandler oAuth2LoginFailureHandler;
    private final ApplicationConfig config;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(request ->
                        request.requestMatchers(permittedEndpoints()).permitAll()
                                .requestMatchers(adminEndpoints()).hasRole("ADMIN")
                                .anyRequest().authenticated())
                .oauth2Login(auth -> auth
                        .successHandler(oAuth2LoginSuccessHandler)
                        .failureHandler(oAuth2LoginFailureHandler)
                )
                .sessionManagement(manager -> manager.sessionCreationPolicy(STATELESS))
                .authenticationProvider(authenticationProvider)
                .exceptionHandling(handler -> handler
                        .authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED))
                )
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    private String[] permittedEndpoints() {
        List<String> endpoints = new ArrayList<>(
                List.of("/api/*/users/auth/*", "/oauth2/**")
        );

        if (!config.getProduction()) {
            endpoints.addAll(
                    List.of("/swagger-ui/**", "/v3/api-docs", "/v3/api-docs/swagger-config")
            );
        }

        return endpoints.toArray(new String[0]);
    }

    private String[] adminEndpoints() {
        return new String[]{
                "/api/*/users/auth/admin/**"
        };
    }

}
