package com.raulbolivar.securitytoken.model;

import com.raulbolivar.securitytoken.exception.ChannelNotConfiguredException;
import com.raulbolivar.securitytoken.exception.DecryptionException;
import com.raulbolivar.securitytoken.exception.MissingParameterException;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

class ModelAndExceptionTest {

    @Test
    void recordsShouldExposeAssignedValues() {
        GenerateTokenCommand command = new GenerateTokenCommand("id", "secret", "gt", "scope", "8");
        DecryptedCredentials credentials = new DecryptedCredentials("id", "secret", "gt", "scope");
        ChannelCryptoConfig config = new ChannelCryptoConfig("8", "k", "i");

        assertEquals("id", command.clientId());
        assertEquals("secret", command.clientSecret());
        assertEquals("gt", command.grantType());
        assertEquals("scope", command.scope());
        assertEquals("8", command.channel());
        assertEquals("id", credentials.clientId());
        assertEquals("8", config.channel());
    }

    @Test
    void exceptionsShouldBuildExpectedMessages() {
        RuntimeException cause = new RuntimeException("boom");

        MissingParameterException missing = new MissingParameterException(List.of("client_id", "scope"));
        ChannelNotConfiguredException channel = new ChannelNotConfiguredException("9");
        DecryptionException decryption = new DecryptionException("client_secret", cause);

        assertEquals("Parámetros faltantes: client_id, scope", missing.getMessage());
        assertEquals("El canal '9' no tiene llave/IV configurados", channel.getMessage());
        assertEquals("No fue posible descifrar el campo 'client_secret'", decryption.getMessage());
        assertSame(cause, decryption.getCause());
    }
}
