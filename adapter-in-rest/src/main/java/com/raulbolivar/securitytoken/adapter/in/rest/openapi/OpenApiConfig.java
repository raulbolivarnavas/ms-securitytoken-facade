package com.raulbolivar.securitytoken.adapter.in.rest.openapi;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Value("${info.app.url}")
    private String url;

    @Bean
    public OpenAPI openAPI(@Value("${info.app.name}") String title,
                           @Value("${info.app.description}") String description,
                           @Value("${info.app.version}") String appVersion) {
        return new OpenAPI()
                .info(new Info()
                        .title(title)
                        .version(appVersion)
                        .description(description)
                        .contact(new Contact()
                                .name("Integration Team Raul Bolivar Services")
                                .email("devops@raulbolivar.com")))
                .servers(List.of(
                        new Server()
                                .url(url)
                                .description("Local"),
                        new Server()
                                .url("https://{environment}.raulbolivar.com")
                                .description("Integration")
                ));
    }
}
