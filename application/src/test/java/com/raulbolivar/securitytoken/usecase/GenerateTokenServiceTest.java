package com.raulbolivar.securitytoken.usecase;

import com.raulbolivar.securitytoken.model.ChannelCryptoConfig;
import com.raulbolivar.securitytoken.model.DecryptedCredentials;
import com.raulbolivar.securitytoken.model.GenerateTokenCommand;
import com.raulbolivar.securitytoken.ports.out.ChannelCryptoConfigPort;
import com.raulbolivar.securitytoken.ports.out.CredentialDecryptorPort;
import com.raulbolivar.securitytoken.ports.out.SsoTokenGatewayPort;
import com.raulbolivar.securitytoken.ports.out.SsoTokenResult;
import com.raulbolivar.securitytoken.service.GenerateTokenDomainService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import reactor.core.publisher.Mono;

@ExtendWith(MockitoExtension.class)
class GenerateTokenServiceTest {

    @Mock
    private ChannelCryptoConfigPort channelCryptoConfigPort;
    @Mock
    private SsoTokenGatewayPort ssoTokenGatewayPort;
    @Mock
    private CredentialDecryptorPort decryptor;
    @Mock
    private GenerateTokenDomainService domainService;

    @InjectMocks
    private GenerateTokenService service;

    @Test
    void executeShouldResolveAndRequestToken() {
        GenerateTokenCommand command = new GenerateTokenCommand("a", "b", "c", "d", "8");
        ChannelCryptoConfig cfg = new ChannelCryptoConfig("8", "k", "i");
        DecryptedCredentials decrypted = new DecryptedCredentials("id", "secret", "gt", "scope");
        SsoTokenResult expected = new SsoTokenResult(200, "{\"access_token\":\"x\"}");

        when(channelCryptoConfigPort.resolve("8")).thenReturn(cfg);
        when(domainService.prepareDecryptedCredentials(eq(command), eq(cfg), any())).thenReturn(decrypted);
        when(ssoTokenGatewayPort.requestToken(decrypted)).thenReturn(Mono.just(expected));

        SsoTokenResult result = service.execute(command).block();

        assertSame(expected, result);
        verify(channelCryptoConfigPort).resolve("8");
        verify(domainService).prepareDecryptedCredentials(eq(command), eq(cfg), any());
        verify(ssoTokenGatewayPort).requestToken(decrypted);
    }

    @Test
    void executeShouldPropagateDomainErrors() {
        GenerateTokenCommand command = new GenerateTokenCommand("a", "b", "c", "d", "8");
        RuntimeException expected = new RuntimeException("fail");
        when(channelCryptoConfigPort.resolve("8")).thenThrow(expected);

        RuntimeException result = assertThrows(RuntimeException.class, () -> service.execute(command).block());

        assertSame(expected, result);
    }

    @Test
    void ssoTokenResultRecordShouldExposeValues() {
        SsoTokenResult result = new SsoTokenResult(401, "{\"error\":\"invalid_client\"}");
        assertEquals(401, result.statusCode());
        assertEquals("{\"error\":\"invalid_client\"}", result.body());
    }
}
