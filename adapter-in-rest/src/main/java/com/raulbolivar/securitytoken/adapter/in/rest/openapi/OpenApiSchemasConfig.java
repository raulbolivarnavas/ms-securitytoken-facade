package com.raulbolivar.securitytoken.adapter.in.rest.openapi;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springdoc.core.annotations.RouterOperation;
import org.springdoc.core.annotations.RouterOperations;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Configuration
public class OpenApiSchemasConfig {

    @Bean
    @RouterOperations({
            @RouterOperation(
                    path = OpenApiDynamicConfig.API_PATH,
                    method = RequestMethod.POST,
                    consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE,
                    produces = MediaType.APPLICATION_JSON_VALUE,
                    operation = @Operation(
                            operationId = "generateToken",
                            summary = "Generar token de acceso",
                            description = OpenApiDynamicConfig.API_DESCRIPTION,
                            tags = {"Security Token"},
                            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                                    required = true,
                                    content = @Content(
                                            mediaType = MediaType.APPLICATION_FORM_URLENCODED_VALUE,
                                            schema = @Schema(
                                                    implementation = OpenApiRequestConfig.class
                                            )
                                    )
                            ),
                            responses = {
                                    @ApiResponse(
                                            responseCode = "200",
                                            description = "Token generado correctamente",
                                            content = @Content(
                                                    mediaType = MediaType.APPLICATION_JSON_VALUE
                                            )
                                    ),
                                    @ApiResponse(
                                            responseCode = "400",
                                            description = "Parámetros inválidos o incompletos",
                                            content = @Content(
                                                    mediaType = MediaType.APPLICATION_JSON_VALUE
                                            )
                                    ),
                                    @ApiResponse(
                                            responseCode = "401",
                                            description = "Credenciales inválidas",
                                            content = @Content(
                                                    mediaType = MediaType.APPLICATION_JSON_VALUE
                                            )
                                    ),
                                    @ApiResponse(
                                            responseCode = "500",
                                            description = "Error interno del servicio",
                                            content = @Content(
                                                    mediaType = MediaType.APPLICATION_JSON_VALUE
                                            )
                                    )
                            }
                    )
            )
    })
    public RouterFunction<ServerResponse> documentedRoutes() {
        return route()
                .GET("/__openapi-doc-only", req -> ServerResponse
                        .notFound().build())
                .build();
    }
}
