package com.raulbolivar.securitytoken.service;

import com.raulbolivar.securitytoken.exception.MissingParameterException;
import com.raulbolivar.securitytoken.model.ChannelCryptoConfig;
import com.raulbolivar.securitytoken.model.DecryptedCredentials;
import com.raulbolivar.securitytoken.model.GenerateTokenCommand;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class GenerateTokenDomainServiceTest {

    private final GenerateTokenDomainService service = new GenerateTokenDomainService();

    @Test
    void prepareDecryptedCredentialsShouldDecryptAllFields() {
        GenerateTokenCommand command = new GenerateTokenCommand("encId", "encSecret", "encGrant", "encScope", "8");
        ChannelCryptoConfig cfg = new ChannelCryptoConfig("8", "12345678901234567890123456789012", "1234567890123456");

        DecryptedCredentials result = service.prepareDecryptedCredentials(command, cfg,
                (field, payload, config) -> field + "-" + payload + "-" + config.channel());

        assertEquals("client_id-encId-8", result.clientId());
        assertEquals("client_secret-encSecret-8", result.clientSecret());
        assertEquals("grant_type-encGrant-8", result.grantType());
        assertEquals("scope-encScope-8", result.scope());
    }

    @Test
    void prepareDecryptedCredentialsShouldFailWhenRequiredFieldsAreMissing() {
        GenerateTokenCommand command = new GenerateTokenCommand(" ", null, "", "scope", " ");
        ChannelCryptoConfig cfg = new ChannelCryptoConfig("8", "12345678901234567890123456789012", "1234567890123456");

        MissingParameterException exception = assertThrows(
                MissingParameterException.class,
                () -> service.prepareDecryptedCredentials(command, cfg, (field, payload, config) -> payload)
        );

        assertEquals("Parámetros faltantes: client_id, client_secret, grant_type, channel", exception.getMessage());
    }
}
