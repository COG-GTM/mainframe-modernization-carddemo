package com.carddemo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Spring Boot entry point for the modernized CardDemo interest calculator (CBACT04C).
 *
 * <p>Run the batch job from the command line with a processing date, e.g.:
 * <pre>mvn spring-boot:run -Dspring-boot.run.arguments=--processingDate=2022071800</pre>
 */
@SpringBootApplication
public class CardDemoInterestApplication {

    public static void main(String[] args) {
        SpringApplication.run(CardDemoInterestApplication.class, args);
    }
}
