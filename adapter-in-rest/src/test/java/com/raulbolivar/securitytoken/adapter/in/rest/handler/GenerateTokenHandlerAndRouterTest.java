package com.raulbolivar.securitytoken.adapter.in.rest.handler;

import com.raulbolivar.securitytoken.adapter.in.rest.RouterRest;
import com.raulbolivar.securitytoken.adapter.in.rest.mapper.GenerateTokenApiMapper;
import com.raulbolivar.securitytoken.exception.ChannelNotConfiguredException;
import com.raulbolivar.securitytoken.exception.DecryptionException;
import com.raulbolivar.securitytoken.exception.MissingParameterException;
import com.raulbolivar.securitytoken.model.GenerateTokenCommand;
import com.raulbolivar.securitytoken.ports.in.GenerateTokenUseCase;
import com.raulbolivar.securitytoken.ports.out.SsoTokenResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.util.MultiValueMap;
import reactor.core.publisher.Mono;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class GenerateTokenHandlerAndRouterTest {

    private GenerateTokenUseCase useCase;
    private GenerateTokenApiMapper mapper;
    private WebTestClient webTestClient;
    private GenerateTokenCommand command;

    @BeforeEach
    void setUp() {
        useCase = Mockito.mock(GenerateTokenUseCase.class);
        mapper = Mockito.mock(GenerateTokenApiMapper.class);
        command = new GenerateTokenCommand("enc-id", "enc-secret", "enc-grant", "enc-scope", "8");

        when(mapper.toCommand(any(MultiValueMap.class))).thenReturn(command);
        GenerateTokenHandler handler = new GenerateTokenHandler(useCase, mapper);
        webTestClient = WebTestClient.bindToRouterFunction(new RouterRest(handler).route()).build();
    }

    @Test
    void shouldReturnSsoPassthroughResponse() {
        when(useCase.execute(command)).thenReturn(Mono.just(new SsoTokenResult(200, "{\"access_token\":\"abc\"}")));

        webTestClient.post()
                .uri("/auth/realms/api-ext-dev/protocol/openid-connect/token")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .bodyValue("client_id=1")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(String.class).isEqualTo("{\"access_token\":\"abc\"}");
    }

    @Test
    void shouldReturnBadRequestWhenMissingParameters() {
        when(useCase.execute(command)).thenReturn(Mono.error(new MissingParameterException(List.of("client_id"))));

        webTestClient.post()
                .uri("/auth/realms/api-ext-dev/protocol/openid-connect/token")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .bodyValue("client_id=1")
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.error").isEqualTo("missing_parameter");
    }

    @Test
    void shouldReturnBadRequestWhenChannelIsNotConfigured() {
        when(useCase.execute(command)).thenReturn(Mono.error(new ChannelNotConfiguredException("8")));

        webTestClient.post()
                .uri("/auth/realms/api-ext-dev/protocol/openid-connect/token")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .bodyValue("client_id=1")
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.error").isEqualTo("channel_not_configured");
    }

    @Test
    void shouldReturnBadRequestWhenDecryptionFails() {
        when(useCase.execute(command)).thenReturn(Mono.error(new DecryptionException("client_id", new RuntimeException("bad"))));

        webTestClient.post()
                .uri("/auth/realms/api-ext-dev/protocol/openid-connect/token")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .bodyValue("client_id=1")
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.error").isEqualTo("decryption_error");
    }

    @Test
    void shouldReturnInternalServerErrorForUnexpectedFailure() {
        when(useCase.execute(command)).thenReturn(Mono.error(new RuntimeException("boom")));

        webTestClient.post()
                .uri("/auth/realms/api-ext-dev/protocol/openid-connect/token")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .bodyValue("client_id=1")
                .exchange()
                .expectStatus().is5xxServerError()
                .expectBody()
                .jsonPath("$.error").isEqualTo("server_error");
    }

    @Test
    void routeShouldRejectNonFormContentType() {
        webTestClient.post()
                .uri("/auth/realms/api-ext-dev/protocol/openid-connect/token")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"x\":1}")
                .exchange()
                .expectStatus().isNotFound();
    }
}
