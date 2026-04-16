package com.carddemo.transaction.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "CardDemo Transaction Service API",
                version = "1.0.0",
                description = "Java/Spring Boot port of CardDemo COBOL transaction processing programs: " +
                        "COTRN00C (List), COTRN01C (View), COTRN02C (Add), COBIL00C (Bill Payment)"
        )
)
public class OpenApiConfig {
}
