package uk.co.nationwide.cards.posting.config;

import java.time.Clock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Exposes a {@link Clock} bean so business logic that depends on "now" is
 * deterministically testable. Replaces the COBOL paragraph
 * <code>Z-GET-DB2-FORMAT-TIMESTAMP</code> which read the system clock directly.
 */
@Configuration
public class ClockConfig {

    @Bean
    public Clock clock() {
        return Clock.systemUTC();
    }
}
