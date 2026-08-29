package com.raulbolivar.securitytoken.adapter.in.rest;

import com.raulbolivar.securitytoken.adapter.in.rest.handler.GenerateTokenHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

@Configuration
@RequiredArgsConstructor
public class RouterRest {

    private final GenerateTokenHandler generateTokenHandler;

    @Bean
    public RouterFunction<ServerResponse> route() {
        return RouterFunctions.route()
                .POST("/auth/realms/{realm}/protocol/openid-connect/token",
                        request -> request.headers()
                                .contentType()
                                .map(MediaType.APPLICATION_FORM_URLENCODED::includes)
                                .orElse(false),
                        generateTokenHandler::handle
                )
                .build();
    }
}
