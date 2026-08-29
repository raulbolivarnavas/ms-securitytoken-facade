package com.raulbolivar.securitytoken.config;

import com.raulbolivar.securitytoken.helpers.AesCbcDecryptor;
import com.raulbolivar.securitytoken.ports.in.GenerateTokenUseCase;
import com.raulbolivar.securitytoken.ports.out.ChannelCryptoConfigPort;
import com.raulbolivar.securitytoken.ports.out.CredentialDecryptorPort;
import com.raulbolivar.securitytoken.ports.out.SsoTokenGatewayPort;
import com.raulbolivar.securitytoken.service.GenerateTokenDomainService;
import com.raulbolivar.securitytoken.sso.adapter.SsoWebClientAdapter;
import com.raulbolivar.securitytoken.sso.config.ChannelCryptoConfigAdapter;
import com.raulbolivar.securitytoken.sso.config.ChannelCryptoProperties;
import com.raulbolivar.securitytoken.usecase.GenerateTokenService;
import io.github.raulbolivarnavas.supportlogging.model.SupportLogCapture;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.web.reactive.function.client.WebClient;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;

class BeanConfigTest {

    private final BeanConfig beanConfig = new BeanConfig();

    @Test
    void shouldBuildCredentialDecryptorPortBean() {
        CredentialDecryptorPort bean = beanConfig.credentialDecryptorPort();
        assertInstanceOf(AesCbcDecryptor.class, bean);
    }

    @Test
    void shouldBuildChannelConfigPortBean() {
        ChannelCryptoConfigPort bean = beanConfig.channelCryptoConfigPort(new ChannelCryptoProperties());
        assertInstanceOf(ChannelCryptoConfigAdapter.class, bean);
    }

    @Test
    void shouldBuildSsoGatewayBean() {
        ChannelCryptoProperties properties = new ChannelCryptoProperties();
        properties.getSso().setUrl("http://localhost/token");

        SsoTokenGatewayPort bean = beanConfig.ssoTokenGatewayPort(
                WebClient.builder().build(),
                properties,
                Mockito.mock(SupportLogCapture.class)
        );

        assertInstanceOf(SsoWebClientAdapter.class, bean);
    }

    @Test
    void shouldBuildDomainServiceBean() {
        assertInstanceOf(GenerateTokenDomainService.class, beanConfig.generateTokenDomainService());
    }

    @Test
    void shouldBuildGenerateTokenUseCaseBean() {
        GenerateTokenUseCase bean = beanConfig.generateTokenUseCase(
                Mockito.mock(ChannelCryptoConfigPort.class),
                Mockito.mock(SsoTokenGatewayPort.class),
                Mockito.mock(CredentialDecryptorPort.class),
                new GenerateTokenDomainService()
        );

        assertInstanceOf(GenerateTokenService.class, bean);
    }
}
