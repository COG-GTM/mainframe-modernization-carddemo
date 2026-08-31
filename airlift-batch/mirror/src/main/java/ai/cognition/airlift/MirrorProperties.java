package ai.cognition.airlift;

import java.nio.file.Path;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * The mirror's equivalent of the JCL DD statements: where the datasets live, plus
 * the two values the JCL supplies as parameters.
 *
 * @param copybookDir directory holding the CardDemo copybooks (app/cpy)
 * @param in directory holding the input datasets the loader reads
 * @param out directory the mirror writes its datasets to
 * @param clock the run timestamp, in the 21-character FUNCTION CURRENT-DATE shape
 *     the COBOL harness is given through AIRLIFT_CLOCK
 * @param parmDate the PARM= value INTCALC passes to CBACT04C
 */
@ConfigurationProperties(prefix = "airlift")
public record MirrorProperties(Path copybookDir, Path in, Path out, String clock, String parmDate) {}
