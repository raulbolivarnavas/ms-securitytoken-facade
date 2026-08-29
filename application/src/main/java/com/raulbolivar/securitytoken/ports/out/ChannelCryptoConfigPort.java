package com.raulbolivar.securitytoken.ports.out;

import com.raulbolivar.securitytoken.exception.ChannelNotConfiguredException;
import com.raulbolivar.securitytoken.model.ChannelCryptoConfig;

public interface ChannelCryptoConfigPort {

    /**
     * @throws ChannelNotConfiguredException
     *         si el canal no tiene llave/IV configurados
     */
    ChannelCryptoConfig resolve(String channel);
}
