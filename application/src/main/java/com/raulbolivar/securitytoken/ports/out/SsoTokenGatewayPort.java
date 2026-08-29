package com.raulbolivar.securitytoken.ports.out;

import com.raulbolivar.securitytoken.model.DecryptedCredentials;
import reactor.core.publisher.Mono;

public interface SsoTokenGatewayPort {

    Mono<SsoTokenResult> requestToken(DecryptedCredentials credentials);
}
