package com.raulbolivar.securitytoken.adapter.in.rest.openapi;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.Paths;
import io.swagger.v3.oas.models.examples.Example;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class OpenApiDynamicConfigTest {

    @Test
    void openApiCustomizerShouldReplacePathAndPlaceholders() {
        OpenApiDynamicConfig config = new OpenApiDynamicConfig();
        ReflectionTestUtils.setField(config, "errorBaseUrl", "https://errors.local");
        ReflectionTestUtils.setField(config, "infoAppDescription", "desc-dinamic");
        ReflectionTestUtils.setField(config, "basePath", "/auth/realms");

        Operation operation = new Operation()
                .description("D " + OpenApiDynamicConfig.API_DESCRIPTION)
                .summary("S {{API_SUMMARY}}");
        Example example = new Example().value("See " + OpenApiDynamicConfig.ERROR_BASE_URL);
        MediaType mediaType = new MediaType().examples(new LinkedHashMap<>(Map.of("e1", example)));
        operation.responses(new ApiResponses()
                .addApiResponse("400", new ApiResponse().content(new Content().addMediaType("application/json", mediaType))));

        OpenAPI openAPI = new OpenAPI();
        openAPI.setPaths(new Paths().addPathItem(OpenApiDynamicConfig.API_PATH, new PathItem().post(operation)));

        config.openApiCustomizer().customise(openAPI);

        PathItem resolvedPath = openAPI.getPaths().get("/auth/realms/api-ext-dev/protocol/openid-connect/token");
        assertNotNull(resolvedPath);
        assertNull(openAPI.getPaths().get(OpenApiDynamicConfig.API_PATH));
        assertEquals("D desc-dinamic", resolvedPath.getPost().getDescription());
        assertEquals("S desc-dinamic", resolvedPath.getPost().getSummary());
        assertEquals("See https://errors.local",
                resolvedPath.getPost().getResponses().get("400").getContent().get("application/json").getExamples().get("e1").getValue());
    }

    @Test
    void openApiCustomizerShouldHandleNullPaths() {
        OpenApiDynamicConfig config = new OpenApiDynamicConfig();
        ReflectionTestUtils.setField(config, "errorBaseUrl", "https://errors.local");
        ReflectionTestUtils.setField(config, "infoAppDescription", "desc-dinamic");
        ReflectionTestUtils.setField(config, "basePath", "/auth/realms");

        OpenAPI openAPI = new OpenAPI();

        config.openApiCustomizer().customise(openAPI);

        assertNull(openAPI.getPaths());
    }
}
