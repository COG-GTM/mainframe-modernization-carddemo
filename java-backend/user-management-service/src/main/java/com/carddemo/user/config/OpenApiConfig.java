package com.carddemo.user.config;

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
 * OpenAPI/Swagger Configuration for User Management Service
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("CardDemo User Management Service API")
                        .version("1.0.0")
                        .description("""
                                User Management Service for the CardDemo Application.
                                
                                This service replaces the following COBOL programs from the mainframe 
                                CardDemo application as part of Phase 1 of the Strangler Fig Pattern migration:
                                
                                - COUSR00C (Transaction CU00) - List all users
                                - COUSR01C (Transaction CU01) - Add new user
                                - COUSR02C (Transaction CU02) - Update user
                                - COUSR03C (Transaction CU03) - Delete user
                                
                                All operations work with the USRSEC data (originally VSAM KSDS file).
                                
                                Access Control:
                                - All endpoints require ADMIN role
                                - Matches mainframe behavior where user management is only accessible from Admin menu
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
                                        .description("JWT token obtained from Authentication Service /api/auth/login")));
    }
}
