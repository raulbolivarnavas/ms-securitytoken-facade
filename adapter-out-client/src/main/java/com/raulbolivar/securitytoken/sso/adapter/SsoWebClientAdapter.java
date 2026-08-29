package com.raulbolivar.securitytoken.sso.adapter;

import com.raulbolivar.securitytoken.model.DecryptedCredentials;
import com.raulbolivar.securitytoken.ports.out.SsoTokenGatewayPort;
import com.raulbolivar.securitytoken.ports.out.SsoTokenResult;
import io.github.raulbolivarnavas.supportlogging.SupportLogging;
import io.github.raulbolivarnavas.supportlogging.model.SupportLogCapture;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Map;

@Slf4j
public class SsoWebClientAdapter implements SsoTokenGatewayPort {

    private final WebClient webClient;
    private final String ssoUrl;
    private final SupportLogCapture supportLogCapture;

    public SsoWebClientAdapter(WebClient webClient,
                               String ssoUrl,
                               SupportLogCapture supportLogCapture) {
        this.webClient = webClient;
        this.ssoUrl = ssoUrl;
        this.supportLogCapture = supportLogCapture;
    }

    @Override
    @SupportLogging(operation = "get-token")
    public Mono<SsoTokenResult> requestToken(DecryptedCredentials credentials) {
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("client_id", credentials.clientId());
        form.add("client_secret", credentials.clientSecret());
        form.add("grant_type", credentials.grantType());
        form.add("scope", credentials.scope());

        log.info("Requesting token from SSO at {} with client_id={} grant_type={} scope={}",
                ssoUrl, credentials.clientId(), credentials.grantType(), credentials.scope());

        Map<String, String> headers = Map.of("Content-Type", "application/x-www-form-urlencoded");

        return supportLogCapture.request("POST", ssoUrl, Map.of(), headers, null)
                .then(webClient.post()
                        .uri(ssoUrl)
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .body(BodyInserters.fromFormData(form))
                        // exchangeToMono en vez de retrieve(): queremos el body tanto en 2xx
                        // como en 4xx/5xx para hacer passthrough tal cual devuelve el SSO
                        .exchangeToMono(response -> response.bodyToMono(String.class)
                                .defaultIfEmpty("")
                                .map(body -> new SsoTokenResult(response.statusCode().value(), body))));
    }
}
