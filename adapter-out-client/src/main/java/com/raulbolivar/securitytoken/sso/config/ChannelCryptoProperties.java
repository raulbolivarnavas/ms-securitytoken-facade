package com.raulbolivar.securitytoken.sso.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Map;

/**
 * Mapea security-token.channels.* del application.yml — equivalente a las tablas
 * "ConfigMap"/"Secret" del documento (service.securityToken.llave.{n} / iv.{n}).
 * La llave/iv de cada canal deberían vivir en un Secret real (K8s Secret / Vault),
 * no en el ConfigMap.
 */
@Setter
@Getter
@ConfigurationProperties(prefix = "security-token")
public class ChannelCryptoProperties {

    private Map<String, ChannelEntry> channels;
    private Sso sso = new Sso();

    @Setter
    @Getter
    public static class ChannelEntry {
        private String llave;
        private String iv;

    }

    @Setter
    @Getter
    public static class Sso {
        private String url;
        private long timeoutMs = 1500;

    }
}
