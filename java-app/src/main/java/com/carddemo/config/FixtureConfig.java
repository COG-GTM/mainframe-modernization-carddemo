package com.carddemo.config;

import com.carddemo.fixture.CardDemoDataSet;
import com.carddemo.fixture.FixedWidthFixtureLoader;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Publishes the canonical mock dataset parsed from the sample fixed-width files. */
@Configuration
public class FixtureConfig {

    @Bean
    public CardDemoDataSet cardDemoDataSet(FixedWidthFixtureLoader loader) {
        return loader.loadAll();
    }
}
