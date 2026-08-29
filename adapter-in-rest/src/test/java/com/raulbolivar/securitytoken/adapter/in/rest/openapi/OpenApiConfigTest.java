package com.raulbolivar.securitytoken.adapter.in.rest.openapi;

import io.swagger.v3.oas.models.OpenAPI;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;

class OpenApiConfigTest {

    @Test
    void openApiShouldContainConfiguredInfoAndServers() {
        OpenApiConfig config = new OpenApiConfig();
        ReflectionTestUtils.setField(config, "url", "http://localhost:8081");

        OpenAPI openAPI = config.openAPI("Security Token", "desc", "1.0.0");

        assertEquals("Security Token", openAPI.getInfo().getTitle());
        assertEquals("desc", openAPI.getInfo().getDescription());
        assertEquals("1.0.0", openAPI.getInfo().getVersion());
        assertEquals("http://localhost:8081", openAPI.getServers().get(0).getUrl());
        assertEquals("https://{environment}.raulbolivar.com", openAPI.getServers().get(1).getUrl());
    }
}
