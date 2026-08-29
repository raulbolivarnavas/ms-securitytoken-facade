package com.raulbolivar.securitytoken.sso.config;

import com.raulbolivar.securitytoken.exception.ChannelNotConfiguredException;
import com.raulbolivar.securitytoken.model.ChannelCryptoConfig;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ChannelCryptoConfigAdapterTest {

    @Test
    void resolveShouldReturnChannelConfiguration() {
        ChannelCryptoProperties properties = new ChannelCryptoProperties();
        ChannelCryptoProperties.ChannelEntry entry = new ChannelCryptoProperties.ChannelEntry();
        entry.setLlave("12345678901234567890123456789012");
        entry.setIv("1234567890123456");
        properties.setChannels(Map.of("8", entry));

        ChannelCryptoConfig result = new ChannelCryptoConfigAdapter(properties).resolve("8");

        assertEquals("8", result.channel());
        assertEquals("12345678901234567890123456789012", result.llave());
        assertEquals("1234567890123456", result.iv());
    }

    @Test
    void resolveShouldFailWhenChannelDoesNotExist() {
        ChannelCryptoProperties properties = new ChannelCryptoProperties();
        properties.setChannels(Map.of());

        assertThrows(ChannelNotConfiguredException.class, () -> new ChannelCryptoConfigAdapter(properties).resolve("9"));
    }

    @Test
    void resolveShouldFailWhenChannelsMapIsNull() {
        ChannelCryptoProperties properties = new ChannelCryptoProperties();
        properties.setChannels(null);

        assertThrows(ChannelNotConfiguredException.class, () -> new ChannelCryptoConfigAdapter(properties).resolve("9"));
    }
}
