package com.raulbolivar.securitytoken.usecase;

import com.raulbolivar.securitytoken.model.ChannelCryptoConfig;
import com.raulbolivar.securitytoken.model.DecryptedCredentials;
import com.raulbolivar.securitytoken.model.GenerateTokenCommand;
import com.raulbolivar.securitytoken.ports.in.GenerateTokenUseCase;
import com.raulbolivar.securitytoken.ports.out.ChannelCryptoConfigPort;
import com.raulbolivar.securitytoken.ports.out.CredentialDecryptorPort;
import com.raulbolivar.securitytoken.ports.out.SsoTokenGatewayPort;
import com.raulbolivar.securitytoken.ports.out.SsoTokenResult;
import com.raulbolivar.securitytoken.service.GenerateTokenDomainService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
@RequiredArgsConstructor
public class GenerateTokenService implements GenerateTokenUseCase {

    private final ChannelCryptoConfigPort channelCryptoConfigPort;
    private final SsoTokenGatewayPort ssoTokenGatewayPort;
    private final CredentialDecryptorPort decryptor;
    private final GenerateTokenDomainService domainService;

    @Override
    public Mono<SsoTokenResult> execute(GenerateTokenCommand command) {
        return Mono.fromCallable(() -> {
                    // Paso 3: obtener llave/IV según el canal
                    ChannelCryptoConfig cfg = channelCryptoConfigPort.resolve(command.channel());
                    log.info("Solicitud de token recibida para channel={}", command.channel());

                    // Paso 4: validar y descifrar credenciales en servicio de dominio
                    DecryptedCredentials credentials = domainService
                            .prepareDecryptedCredentials(command, cfg, decryptor::decrypt);

                    log.debug("Campos descifrados correctamente para channel={}", command.channel());
                    return credentials;
                })
                // Paso 5+6: consumir el endpoint de generacion de token y recibir la respuesta (o error)
                .flatMap(ssoTokenGatewayPort::requestToken);
    }
}
