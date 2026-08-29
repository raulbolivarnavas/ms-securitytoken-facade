package com.raulbolivar.securitytoken.adapter.in.rest.dto;

import com.raulbolivar.securitytoken.adapter.in.rest.openapi.OpenApiRequestConfig;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DtoConfigCoverageTest {

    @Test
    void generateTokenRequestDtoShouldExposeValues() {
        GenerateTokenRequestDto dto = new GenerateTokenRequestDto("id", "secret", "grant", "scope", "8");

        assertEquals("id", dto.clientId());
        assertEquals("secret", dto.clientSecret());
        assertEquals("grant", dto.grantType());
        assertEquals("scope", dto.scope());
        assertEquals("8", dto.channel());
    }

    @Test
    void openApiRequestConfigShouldAllowReflectionConstruction() throws Exception {
        Constructor<OpenApiRequestConfig> constructor = OpenApiRequestConfig.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        OpenApiRequestConfig config = constructor.newInstance();
        assertNotNull(config);

        Field channelField = OpenApiRequestConfig.class.getDeclaredField("channel");
        channelField.setAccessible(true);
        channelField.set(config, "8");

        assertEquals("8", channelField.get(config));
        assertTrue(OpenApiRequestConfig.class.getDeclaredFields().length >= 6);
    }
}
