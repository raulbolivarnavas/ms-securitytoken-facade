package com.raulbolivar.securitytoken.ports.in;

import com.raulbolivar.securitytoken.model.GenerateTokenCommand;
import com.raulbolivar.securitytoken.ports.out.SsoTokenResult;
import reactor.core.publisher.Mono;

public interface GenerateTokenUseCase {

    Mono<SsoTokenResult> execute(GenerateTokenCommand command);
}
