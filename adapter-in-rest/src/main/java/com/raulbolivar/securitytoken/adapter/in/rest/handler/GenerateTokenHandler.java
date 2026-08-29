package com.raulbolivar.securitytoken.adapter.in.rest.handler;

import com.raulbolivar.securitytoken.adapter.in.rest.mapper.GenerateTokenApiMapper;
import com.raulbolivar.securitytoken.exception.ChannelNotConfiguredException;
import com.raulbolivar.securitytoken.exception.DecryptionException;
import com.raulbolivar.securitytoken.exception.MissingParameterException;
import com.raulbolivar.securitytoken.ports.in.GenerateTokenUseCase;
import com.raulbolivar.securitytoken.ports.out.SsoTokenResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class GenerateTokenHandler {

    private final GenerateTokenUseCase generateTokenUseCase;
    private final GenerateTokenApiMapper mapper;

    public Mono<ServerResponse> handle(ServerRequest request) {

        return request.formData()
                .map(mapper::toCommand)
                .doOnNext(command ->
                        log.info(
                                "[GENERATE-TOKEN] Request received clientId={}, grantType={}, scope={}, channel={}",
                                command.clientId(),
                                command.grantType(),
                                command.scope(),
                                command.channel()
                        )
                )
                .flatMap(generateTokenUseCase::execute)
                .flatMap(this::toServerResponse)
                .onErrorResume(MissingParameterException.class, e -> {
                    log.warn("[GENERATE-TOKEN] Parámetros faltantes: {}", e.getMessage());
                    return errorResponse(HttpStatus.BAD_REQUEST, "missing_parameter", e.getMessage());
                })
                .onErrorResume(ChannelNotConfiguredException.class, e -> {
                    log.warn("[GENERATE-TOKEN] Canal no configurado: {}", e.getMessage());
                    return errorResponse(HttpStatus.BAD_REQUEST, "channel_not_configured", e.getMessage());
                })
                .onErrorResume(DecryptionException.class, e -> {
                    log.warn("[GENERATE-TOKEN] Error de descifrado: {}", e.getMessage());
                    return errorResponse(HttpStatus.BAD_REQUEST, "decryption_error", e.getMessage());
                })
                .onErrorResume(e -> {
                    log.error("[GENERATE-TOKEN] Error inesperado", e);
                    return errorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "server_error", "Error interno del servidor");
                });
    }

    private Mono<ServerResponse> errorResponse(HttpStatus status, String errorCode, String description) {
        return ServerResponse.status(status)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(Map.of("error", errorCode, "error_description", description));
    }

    private Mono<ServerResponse> toServerResponse(SsoTokenResult result) {
        return ServerResponse.status(result.statusCode())
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .bodyValue(result.body());
    }
}