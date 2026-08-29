package com.raulbolivar.securitytoken.model;

/**
 * Credenciales ya descifradas, listas para reenviar al SSO real.
 * Nunca cruza hacia el adapter-in (REST) — solo vive entre application y adapter-out-sso.
 */
public record DecryptedCredentials(
        String clientId,
        String clientSecret,
        String grantType,
        String scope
) {
}
