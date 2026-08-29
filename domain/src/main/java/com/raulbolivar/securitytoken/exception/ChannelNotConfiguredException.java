package com.raulbolivar.securitytoken.exception;

public class ChannelNotConfiguredException extends RuntimeException {

    public ChannelNotConfiguredException(String channel) {
        super("El canal '" + channel + "' no tiene llave/IV configurados");
    }
}
