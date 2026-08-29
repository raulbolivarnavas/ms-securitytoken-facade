package com.raulbolivar.securitytoken.sso.adapter;

import com.raulbolivar.securitytoken.model.DecryptedCredentials;
import com.raulbolivar.securitytoken.ports.out.SsoTokenResult;
import io.github.raulbolivarnavas.supportlogging.model.SupportLogCapture;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SsoWebClientAdapterTest {

    @Mock
    private SupportLogCapture supportLogCapture;

    @Test
    void requestTokenShouldReturnPassthroughResult() {
        WebClient webClient = WebClient.builder()
                .exchangeFunction(request -> Mono.just(
                        ClientResponse.create(HttpStatus.OK)
                                .body("{\"access_token\":\"abc\"}")
                                .build()))
                .build();

        when(supportLogCapture.request(anyString(), anyString(), anyMap(), anyMap(), isNull()))
                .thenReturn(Mono.empty());

        SsoWebClientAdapter adapter = new SsoWebClientAdapter(webClient, "http://localhost/token", supportLogCapture);
        DecryptedCredentials credentials = new DecryptedCredentials("id", "secret", "client_credentials", "openid");

        SsoTokenResult result = adapter.requestToken(credentials).block();

        assertEquals(200, result.statusCode());
        assertEquals("{\"access_token\":\"abc\"}", result.body());
        verify(supportLogCapture).request(anyString(), anyString(), anyMap(), anyMap(), isNull());
    }

    @Test
    void requestTokenShouldSupportEmptyBodyResponses() {
        WebClient webClient = WebClient.builder()
                .exchangeFunction(request -> Mono.just(
                        ClientResponse.create(HttpStatus.UNAUTHORIZED).build()))
                .build();

        when(supportLogCapture.request(anyString(), anyString(), anyMap(), anyMap(), isNull()))
                .thenReturn(Mono.empty());

        SsoWebClientAdapter adapter = new SsoWebClientAdapter(webClient, "http://localhost/token", supportLogCapture);
        DecryptedCredentials credentials = new DecryptedCredentials("id", "secret", "client_credentials", "openid");

        SsoTokenResult result = adapter.requestToken(credentials).block();

        assertEquals(401, result.statusCode());
        assertEquals("", result.body());
    }
}
