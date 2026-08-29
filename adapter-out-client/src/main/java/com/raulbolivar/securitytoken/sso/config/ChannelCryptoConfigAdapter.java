package com.raulbolivar.securitytoken.sso.config;

import com.raulbolivar.securitytoken.exception.ChannelNotConfiguredException;
import com.raulbolivar.securitytoken.model.ChannelCryptoConfig;
import com.raulbolivar.securitytoken.ports.out.ChannelCryptoConfigPort;

public class ChannelCryptoConfigAdapter implements ChannelCryptoConfigPort {

    private final ChannelCryptoProperties properties;

    public ChannelCryptoConfigAdapter(ChannelCryptoProperties properties) {
        this.properties = properties;
    }

    @Override
    public ChannelCryptoConfig resolve(String channel) {
        ChannelCryptoProperties.ChannelEntry entry =
                properties.getChannels() != null ? properties.getChannels().get(channel) : null;

        if (entry == null) {
            throw new ChannelNotConfiguredException(channel);
        }

        return new ChannelCryptoConfig(channel, entry.getLlave(), entry.getIv());
    }
}
