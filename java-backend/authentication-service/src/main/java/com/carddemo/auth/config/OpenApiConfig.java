package com.carddemo.auth.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI/Swagger Configuration
 * 
 * Provides API documentation for the Authentication Service.
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
                                Authentication Service for the CardDemo Application.
                                
                                This service replaces the COSGN00C COBOL program (Transaction CC00) 
                                from the mainframe CardDemo application as part of Phase 1 of the 
                                Strangler Fig Pattern migration.
                                
                                Original COBOL Program Functions:
                                - User login/logout (COSGN00C)
                                - Credential validation against USRSEC VSAM file
                                - Session management via CARDDEMO-COMMAREA
                                
                                Modernized Implementation:
                                - JWT-based authentication
                                - RESTful API endpoints
                                - BCrypt password hashing
                                """)
                        .contact(new Contact()
                                .name("CardDemo Modernization Team")
                                .email("carddemo@example.com"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0")))
                .addSecurityItem(new SecurityRequirement().addList("Bearer Authentication"))
                .components(new Components()
                        .addSecuritySchemes("Bearer Authentication",
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("JWT token obtained from /api/auth/login")));
    }
}
