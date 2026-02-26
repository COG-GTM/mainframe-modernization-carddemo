package com.carddemo.transaction.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI / Swagger configuration for API documentation.
 * Provides interactive API documentation at /swagger-ui.html.
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("CardDemo Transaction Service API")
                        .version("1.0.0")
                        .description("REST API for credit card transaction management. " +
                                "Migrated from COBOL/CICS programs COTRN00C, COTRN01C, and COTRN02C " +
                                "in the CardDemo mainframe application.")
                        .contact(new Contact()
                                .name("CardDemo Team"))
                        .license(new License()
                                .name("Apache License 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0")));
    }
}
