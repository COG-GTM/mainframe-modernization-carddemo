package com.carddemo.user.config;

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
                        .title("CardDemo User Management Service API")
                        .version("1.0.0")
                        .description("""
                                User Management Service for CardDemo Mainframe Modernization.
                                
                                This service replaces the COBOL programs:
                                - COUSR00C (CU00) - List all users from USRSEC file
                                - COUSR01C (CU01) - Add a new Regular/Admin user
                                - COUSR02C (CU02) - Update a user
                                - COUSR03C (CU03) - Delete a user
                                
                                Original COBOL functionality:
                                - CRUD operations on USRSEC VSAM file
                                - Paginated user listing (10 records per page)
                                - User selection for update (U) or delete (D)
                                - Field validation
                                
                                New implementation:
                                - RESTful API endpoints
                                - JPA/PostgreSQL persistence
                                - Spring Security authorization
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
