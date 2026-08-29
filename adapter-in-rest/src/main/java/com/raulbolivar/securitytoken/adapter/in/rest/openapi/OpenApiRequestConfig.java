package com.raulbolivar.securitytoken.adapter.in.rest.openapi;

import io.swagger.v3.oas.annotations.media.Schema;

public class OpenApiRequestConfig {

    @Schema(
            name = "client_id",
            description = "Identificador cifrado del cliente",
            requiredMode = Schema.RequiredMode.REQUIRED,
            example = "189cc7f814338a20c863679019ac29704oxMDXmttsJffy+0r66/Og=="
    )
    public String clientId;

    @Schema(
            name = "client_secret",
            description = "Secreto cifrado del cliente",
            requiredMode = Schema.RequiredMode.REQUIRED,
            example = "189cc7f814338a20c863679019ac2970ythg4FvG5dgIlFHBH4Ekpl04Tx2j7IPIhm4IusyipoU="
    )
    public String clientSecret;

    @Schema(
            name = "grant_type",
            description = "Grant type cifrado",
            requiredMode = Schema.RequiredMode.REQUIRED,
            example = "189cc7f814338a20c863679019ac29701/t7jyv57CVdbHSBdNz5RTEP7PVFDuqgOYiYIDyjgLk="
    )
    public String grantType;

    @Schema(
            name = "scope",
            description = "Scope cifrado solicitado",
            requiredMode = Schema.RequiredMode.REQUIRED,
            example = "189cc7f814338a20c863679019ac2970eDlC7df7TTCiEd38bpfe0Q=="
    )
    public String scope;

    @Schema(
            name = "salt",
            description = "Salt utilizado para el proceso criptográfico",
            requiredMode = Schema.RequiredMode.REQUIRED,
            example = "189cc7f814338a20c863679019ac2970"
    )
    public String salt;

    @Schema(
            name = "channel",
            description = "Canal desde el cual se solicita el token",
            requiredMode = Schema.RequiredMode.REQUIRED,
            example = "8"
    )
    public String channel;

    private OpenApiRequestConfig() {
    }
}
