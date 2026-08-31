package ai.cognition.airlift;

import ai.cognition.airlift.codec.Layouts;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

/**
 * The Java mirror of the CardDemo batch slice.
 *
 * <p>One job per JCL job: {@code posttran-intcalc} covers POSTTRAN and INTCALC,
 * {@code readacct} covers READACCT, the untrapped control. Pick one with
 * {@code --spring.batch.job.name=}.
 */
@SpringBootApplication
@EnableConfigurationProperties(MirrorProperties.class)
public class MirrorApplication {

  public static void main(String[] args) {
    SpringApplication.run(MirrorApplication.class, args);
  }

  @Bean
  Layouts layouts(MirrorProperties properties) {
    return new Layouts(properties.copybookDir());
  }
}
