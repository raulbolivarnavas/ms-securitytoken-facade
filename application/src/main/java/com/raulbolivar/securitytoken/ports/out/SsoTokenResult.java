package com.raulbolivar.securitytoken.ports.out;

/**
 * Passthrough del status code + body que devolvió el SSO real (200 con el JWT,
 * o 400/401 con el error), tal como pide el paso 7 del documento:
 * "Se devuelve la respuesta de SSO al cliente".
 */
public record SsoTokenResult(
        int statusCode,
        String body
) {
}
