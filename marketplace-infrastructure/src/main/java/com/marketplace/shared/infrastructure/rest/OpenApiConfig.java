package com.marketplace.shared.infrastructure.rest;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    OpenAPI marketplaceOpenAPI() {
        final String schemeName = "basicAuth";
        return new OpenAPI()
            .info(new Info()
                .title("Marketplace Revente Billets API")
                .description("API modulaire pour revente de billets certifies avec notifications, waitlist et paiements.")
                .version("v1"))
            .addSecurityItem(new SecurityRequirement().addList(schemeName))
            .schemaRequirement(schemeName, new SecurityScheme()
                .name(schemeName)
                .type(SecurityScheme.Type.HTTP)
                .scheme("basic"));
    }
}
