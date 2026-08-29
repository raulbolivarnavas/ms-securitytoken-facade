package com.raulbolivar.securitytoken.adapter.in.rest.dto;

public record GenerateTokenRequestDto(
        String clientId,
        String clientSecret,
        String grantType,
        String scope,
        String channel
) {
}
