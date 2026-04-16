package com.carddemo.reports.service;

import com.carddemo.reports.model.ReportJob;
import com.carddemo.reports.model.ReportStatus;
import com.carddemo.reports.model.ReportType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Handles async report generation.
 * <p>
 * In the original COBOL (CORPT00C), this was done by submitting a JCL batch job
 * via the internal reader (INTRDR). The modern equivalent uses Spring @Async
 * to process report generation asynchronously.
 */
@Service
public class ReportGenerationService {

    private static final Logger log = LoggerFactory.getLogger(ReportGenerationService.class);
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private final Path outputDirectory;

    public ReportGenerationService(
            @Value("${reports.output-directory:#{systemProperties['java.io.tmpdir'] + '/carddemo-reports'}}")
            String outputDirectory) {
        this.outputDirectory = Path.of(outputDirectory);
    }

    @Async
    public void generateReport(ReportJob job) {
        log.info("Starting report generation for job {} (type={}, range={} to {})",
                job.getId(), job.getReportType().displayName(),
                job.getStartDate(), job.getEndDate());

        job.setStatus(ReportStatus.PROCESSING);

        try {
            Files.createDirectories(outputDirectory);

            String fileName = String.format("report_%s_%s.csv",
                    job.getReportType().getCode(), job.getId());
            Path filePath = outputDirectory.resolve(fileName);

            generateCsvReport(job, filePath);

            job.setFilePath(filePath.toString());
            job.setStatus(ReportStatus.COMPLETED);
            job.setCompletedAt(LocalDateTime.now());

            log.info("Report generation completed for job {}: {}", job.getId(), filePath);
        } catch (Exception e) {
            log.error("Report generation failed for job {}", job.getId(), e);
            job.setStatus(ReportStatus.FAILED);
            job.setErrorMessage("Report generation failed: " + e.getMessage());
            job.setCompletedAt(LocalDateTime.now());
        }
    }

    private void generateCsvReport(ReportJob job, Path filePath) throws IOException {
        try (PrintWriter writer = new PrintWriter(Files.newBufferedWriter(filePath))) {
            writer.println("Transaction Report - " + job.getReportType().displayName());
            writer.println("Date Range: " + job.getStartDate().format(DATE_FMT)
                    + " to " + job.getEndDate().format(DATE_FMT));
            writer.println("Job Name: " + job.getJobName());
            writer.println("Generated At: " + LocalDateTime.now());
            writer.println();
            writer.println("Card Number,Transaction Date,Transaction Amount,"
                    + "Merchant,Category,Description");

            LocalDate current = job.getStartDate();
            while (!current.isAfter(job.getEndDate())) {
                int transactionsPerDay = ThreadLocalRandom.current().nextInt(1, 6);
                for (int i = 0; i < transactionsPerDay; i++) {
                    writer.printf("%s,%s,%.2f,%s,%s,%s%n",
                            generateCardNumber(),
                            current.format(DATE_FMT),
                            ThreadLocalRandom.current().nextDouble(5.00, 5000.00),
                            pickRandom(MERCHANTS),
                            pickRandom(CATEGORIES),
                            pickRandom(DESCRIPTIONS));
                }
                current = current.plusDays(1);
            }
        }
    }

    private String generateCardNumber() {
        return String.format("4000-%04d-%04d-%04d",
                ThreadLocalRandom.current().nextInt(1000, 9999),
                ThreadLocalRandom.current().nextInt(1000, 9999),
                ThreadLocalRandom.current().nextInt(1000, 9999));
    }

    private String pickRandom(String[] items) {
        return items[ThreadLocalRandom.current().nextInt(items.length)];
    }

    private static final String[] MERCHANTS = {
            "Amazon", "Walmart", "Target", "Best Buy", "Costco",
            "Home Depot", "Starbucks", "Shell Gas", "Uber", "Grubhub"
    };

    private static final String[] CATEGORIES = {
            "Retail", "Grocery", "Gas", "Dining", "Travel",
            "Entertainment", "Utilities", "Healthcare", "Education", "Services"
    };

    private static final String[] DESCRIPTIONS = {
            "Online purchase", "In-store purchase", "Subscription renewal",
            "Fuel purchase", "Restaurant meal", "Monthly service",
            "Travel booking", "Medical copay", "Course enrollment", "Consulting fee"
    };
}
