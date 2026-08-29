package com.raulbolivar.securitytoken.exception;

import java.util.List;

/**
 * Se lanza cuando falta alguno de los parámetros obligatorios del request:
 * client_id, client_secret, grant_type, scope, channel (paso 2.a del documento).
 */
public class MissingParameterException extends RuntimeException {

    public MissingParameterException(List<String> missingFields) {
        super("Parámetros faltantes: " + String.join(", ", missingFields));
    }
}
