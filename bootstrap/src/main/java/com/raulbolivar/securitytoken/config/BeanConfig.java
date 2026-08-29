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
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class BeanConfig {

    @Bean
    public CredentialDecryptorPort credentialDecryptorPort() {
        return new AesCbcDecryptor();
    }

    @Bean
    public ChannelCryptoConfigPort channelCryptoConfigPort(ChannelCryptoProperties properties) {
        return new ChannelCryptoConfigAdapter(properties);
    }

    @Bean
    public SsoTokenGatewayPort ssoTokenGatewayPort(WebClient ssoWebClient,
                                                   ChannelCryptoProperties properties,
                                                   SupportLogCapture supportLogCapture) {
        return new SsoWebClientAdapter(ssoWebClient, properties.getSso().getUrl(), supportLogCapture);
    }

    @Bean
    public GenerateTokenDomainService generateTokenDomainService() {
        return new GenerateTokenDomainService();
    }

    @Bean
    public GenerateTokenUseCase generateTokenUseCase(ChannelCryptoConfigPort channelCryptoConfigPort,
                                                     SsoTokenGatewayPort ssoTokenGatewayPort,
                                                     CredentialDecryptorPort credentialDecryptorPort,
                                                     GenerateTokenDomainService generateTokenDomainService) {
        return new GenerateTokenService(
                channelCryptoConfigPort,
                ssoTokenGatewayPort,
                credentialDecryptorPort,
                generateTokenDomainService
        );
    }
}
