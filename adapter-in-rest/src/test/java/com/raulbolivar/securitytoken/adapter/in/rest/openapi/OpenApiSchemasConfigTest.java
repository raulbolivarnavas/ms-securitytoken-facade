package com.raulbolivar.securitytoken.adapter.in.rest.openapi;

import org.junit.jupiter.api.Test;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

class OpenApiSchemasConfigTest {

    @Test
    void documentedRoutesShouldExposeOpenApiDocOnlyEndpoint() {
        RouterFunction<ServerResponse> routes = new OpenApiSchemasConfig().documentedRoutes();
        WebTestClient client = WebTestClient.bindToRouterFunction(routes).build();

        client.get()
                .uri("/__openapi-doc-only")
                .exchange()
                .expectStatus().isNotFound();
    }
}
