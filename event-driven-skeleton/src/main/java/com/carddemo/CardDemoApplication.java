package com.carddemo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * CardDemo Event-Driven Application.
 *
 * Replaces the POSTTRAN and CREASTMT batch JCL pipelines with an
 * event-driven architecture using Kafka (or SQS) and Spring Boot.
 */
@SpringBootApplication
public class CardDemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(CardDemoApplication.class, args);
    }
}
