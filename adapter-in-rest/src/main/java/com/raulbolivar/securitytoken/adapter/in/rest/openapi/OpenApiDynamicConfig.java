package com.raulbolivar.securitytoken.adapter.in.rest.openapi;

import lombok.RequiredArgsConstructor;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class OpenApiDynamicConfig {

    @Value("${app.exception.type}")
    private String errorBaseUrl;

    @Value("${info.app.description}")
    private String infoAppDescription;

    @Value("${app.base-path}")
    private String basePath;

    public static final String ERROR_BASE_URL = "{{ERROR_BASE_URL}}";
    public static final String API_DESCRIPTION = "{{API_DESCRIPTION}}";
    public static final String API_PATH = "{{API_PATH}}";

    @Bean
    public OpenApiCustomizer openApiCustomizer() {
        return openApi -> {
            replaceDynamicPath(openApi);
            if (openApi.getPaths() == null) return;

            openApi.getPaths().forEach((path, pathItem) ->
                    pathItem.readOperations().forEach(operation -> {
                        if (operation.getDescription() != null)
                            operation.setDescription(
                                    operation.getDescription()
                                            .replace(
                                                    API_DESCRIPTION,
                                                    infoAppDescription
                                            )
                            );

                        if (operation.getSummary() != null)
                            operation.setSummary(
                                    operation.getSummary()
                                            .replace(
                                                    "{{API_SUMMARY}}",
                                                    infoAppDescription
                                            )
                            );

                        if (operation.getResponses() == null) return;

                        operation.getResponses().forEach((code, response) -> {
                            if (response.getContent() == null
                                    || response.getContent().get("application/json") == null)
                                return;

                            var mediaType = response.getContent().get("application/json");
                            if (mediaType.getExamples() == null) return;

                            mediaType.getExamples().forEach((name, example) -> {
                                Object value = example.getValue();
                                if (value instanceof String text) {
                                    example.setValue(text.replace(
                                            ERROR_BASE_URL,
                                            errorBaseUrl
                                    ));
                                }
                            });
                        });
                    })
            );
        };
    }

    private void replaceDynamicPath(io.swagger.v3.oas.models.OpenAPI openApi) {
        if (openApi.getPaths() == null) return;
        var dynamicPathItem = openApi.getPaths().remove(API_PATH);
        if (dynamicPathItem != null)
            openApi.getPaths()
                    .addPathItem(basePath + "/api-ext-dev/protocol/openid-connect/token", dynamicPathItem);
    }
}
