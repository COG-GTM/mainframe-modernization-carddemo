package com.carddemo.auth.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI/Swagger configuration for API documentation.
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("CardDemo Authentication Service API")
                        .version("1.0.0")
                        .description("""
                                Authentication Service for CardDemo Mainframe Modernization.
                                
                                This service replaces the COBOL program COSGN00C (transaction CC00)
                                which handles user login/logout and validates credentials against
                                the USRSEC VSAM file.
                                
                                Original COBOL functionality:
                                - User ID and password validation
                                - Session management via CICS COMMAREA
                                - Routing to admin (COADM01C) or user (COMEN01C) menus
                                
                                New implementation:
                                - JWT-based authentication
                                - RESTful API endpoints
                                - Stateless session management
                                """)
                        .contact(new Contact()
                                .name("CardDemo Team")
                                .email("carddemo@example.com"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0")))
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"))
                .components(new io.swagger.v3.oas.models.Components()
                        .addSecuritySchemes("bearerAuth", new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")));
    }
}
