package com.raulbolivar.securitytoken.exception;

/**
 * Se lanza cuando un campo no pudo descifrarse ni en modo "con salt" ni en modo
 * legacy "sin salt" — payload corrupto, llave/IV incorrectos, o canal mal configurado.
 */
public class DecryptionException extends RuntimeException {

    public DecryptionException(String field, Throwable cause) {
        super("No fue posible descifrar el campo '" + field + "'", cause);
    }
}
