package com.raulbolivar.securitytoken.config;

import org.junit.jupiter.api.Test;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class SecurityConfigTest {

    @Test
    void securityWebFilterChainShouldBeCreated() {
        SecurityWebFilterChain chain = new SecurityConfig().securityWebFilterChain(ServerHttpSecurity.http());
        assertNotNull(chain);
    }
}
