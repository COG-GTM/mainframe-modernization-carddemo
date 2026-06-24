package com.carddemo.account.batch;

import java.util.Arrays;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * Runs the {@link AccountReportService} (the CBACT01C batch job) at startup when
 * the application is launched with the {@code report} argument or
 * {@code --report} flag, e.g.:
 *
 * <pre>
 *   mvn spring-boot:run -Dspring-boot.run.arguments=report
 *   java -jar account-service.jar report
 * </pre>
 *
 * Without the flag the application simply starts the REST API and does not print
 * the report, so the same artifact serves as both the batch job and the service.
 */
@Component
@Order(20)
public class AccountReportRunner implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(AccountReportRunner.class);

    private final AccountReportService accountReportService;

    public AccountReportRunner(AccountReportService accountReportService) {
        this.accountReportService = accountReportService;
    }

    @Override
    public void run(ApplicationArguments args) {
        boolean reportRequested = args.containsOption("report")
                || args.getNonOptionArgs().stream().anyMatch("report"::equalsIgnoreCase);
        if (!reportRequested) {
            return;
        }
        int count = accountReportService.runReport();
        log.info("CBACT01C report complete: {} account record(s) printed", count);
    }
}
