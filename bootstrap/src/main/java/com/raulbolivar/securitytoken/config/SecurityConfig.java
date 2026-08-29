package com.raulbolivar.securitytoken.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .formLogin(ServerHttpSecurity.FormLoginSpec::disable)
                .httpBasic(ServerHttpSecurity.HttpBasicSpec::disable)

                .authorizeExchange(exchange -> exchange

                        // OAuth2 Token endpoint
                        .pathMatchers(
                                HttpMethod.POST,
                                "/auth/realms/*/protocol/openid-connect/token"
                        ).permitAll()

                        // Kubernetes probes
                        .pathMatchers(
                                "/actuator/health",
                                "/actuator/health/**"
                        ).permitAll()

                        // OpenAPI / Swagger
//                        .pathMatchers(
//                                "/swagger-ui/**",
//                                "/swagger-ui.html",
//                                "/v3/api-docs/**"
//                        ).permitAll()

                        // Todo lo demás requiere autenticación
                        .anyExchange().authenticated()
                )

                .build();
    }
}
