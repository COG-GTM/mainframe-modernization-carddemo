package com.carddemo.cardservice.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI cardServiceOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("CardDemo Card Service API")
                        .description("Credit card management REST API ported from "
                                + "COBOL/CICS CardDemo application. "
                                + "Implements COCRDLIC (list), COCRDSLC (detail), "
                                + "and COCRDUPC (update) programs.")
                        .version("1.0.0")
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0")));
    }
}
