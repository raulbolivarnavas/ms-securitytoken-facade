package com.raulbolivar.securitytoken.model;

/**
 * Los 5 campos que exige el paso 2.a del documento, tal como llegan del cliente
 * (client_id/client_secret/grant_type/scope aún cifrados, channel en claro).
 */
public record GenerateTokenCommand(
        String clientId,
        String clientSecret,
        String grantType,
        String scope,
        String channel
) {
}
