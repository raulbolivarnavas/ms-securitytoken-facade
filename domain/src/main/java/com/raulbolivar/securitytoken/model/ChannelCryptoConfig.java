package com.raulbolivar.securitytoken.model;

/**
 * Llave (32 chars → clave AES-256 directa en modo legacy, o master key para PBKDF2
 * en modo con-salt) e IV (16 chars) configurados para un canal específico.
 * Equivalente a service.securityToken.llave.{n} / service.securityToken.iv.{n}
 * del configmap/secret descritos en el documento de diseño.
 */
public record ChannelCryptoConfig(
        String channel,
        String llave,
        String iv
) {
}
