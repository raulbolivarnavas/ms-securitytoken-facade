package com.raulbolivar.securitytoken.sso.config;

import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.WebClient;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class WebClientConfigTest {

    @Test
    void ssoWebClientShouldBuildClientWithConfiguredTimeout() {
        ChannelCryptoProperties properties = new ChannelCryptoProperties();
        ChannelCryptoProperties.Sso sso = new ChannelCryptoProperties.Sso();
        sso.setUrl("http://localhost:8080/token");
        sso.setTimeoutMs(2500L);
        properties.setSso(sso);

        WebClient client = new WebClientConfig().ssoWebClient(properties);

        assertNotNull(client);
    }

    @Test
    void channelCryptoPropertiesShouldHoldValues() {
        ChannelCryptoProperties properties = new ChannelCryptoProperties();
        ChannelCryptoProperties.ChannelEntry entry = new ChannelCryptoProperties.ChannelEntry();
        entry.setLlave("k");
        entry.setIv("i");
        properties.setChannels(java.util.Map.of("8", entry));

        assertEquals("k", properties.getChannels().get("8").getLlave());
        assertEquals("i", properties.getChannels().get("8").getIv());
        assertNotNull(properties.getSso());
    }

    private static void assertEquals(String expected, String actual) {
        org.junit.jupiter.api.Assertions.assertEquals(expected, actual);
    }
}
