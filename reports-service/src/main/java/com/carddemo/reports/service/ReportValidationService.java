package com.carddemo.reports.service;

import com.carddemo.reports.dto.ReportRequest;
import com.carddemo.reports.exception.ReportValidationException;
import com.carddemo.reports.model.ReportType;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

/**
 * Validates report requests according to the rules from CORPT00C.cbl.
 * <p>
 * The COBOL program validates:
 * - Report type must be 01, 02, or 03
 * - Monthly (01): requires valid month (1-12) and numeric year
 * - Yearly (02): requires numeric year
 * - Custom (03): requires start and end dates with component validation
 *   - Month: 1-12 range
 *   - Day: 1-31 range (no month-specific validation per COBOL spec)
 *   - Year: must be numeric
 */
@Service
public class ReportValidationService {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    /**
     * Validates the report request and returns the computed date range.
     *
     * @param request the report request to validate
     * @return a DateRange with the computed start and end dates
     * @throws ReportValidationException if validation fails
     */
    public DateRange validateAndComputeDateRange(ReportRequest request) {
        if (request.getReportType() == null || request.getReportType().isBlank()) {
            throw new ReportValidationException("Please enter Report Type ...");
        }

        ReportType reportType = ReportType.fromCode(request.getReportType());
        if (reportType == null) {
            throw new ReportValidationException("Report Type must be 01, 02, or 03");
        }

        return switch (reportType) {
            case MONTHLY -> validateMonthly(request);
            case YEARLY -> validateYearly(request);
            case CUSTOM -> validateCustom(request);
        };
    }

    private DateRange validateMonthly(ReportRequest request) {
        List<String> errors = new ArrayList<>();

        if (request.getMonth() == null) {
            errors.add("Please enter a valid start month ...");
        } else if (request.getMonth() < 1 || request.getMonth() > 12) {
            errors.add("Please enter a valid start month ...");
        }

        if (request.getYear() == null) {
            errors.add("Please enter a valid start year ...");
        }

        if (!errors.isEmpty()) {
            throw new ReportValidationException(errors);
        }

        int year = request.getYear();
        int month = request.getMonth();

        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = startDate.plusMonths(1).minusDays(1);

        return new DateRange(startDate, endDate);
    }

    private DateRange validateYearly(ReportRequest request) {
        if (request.getYear() == null) {
            throw new ReportValidationException("Please enter a valid start year ...");
        }

        int year = request.getYear();
        LocalDate startDate = LocalDate.of(year, 1, 1);
        LocalDate endDate = LocalDate.of(year, 12, 31);

        return new DateRange(startDate, endDate);
    }

    private DateRange validateCustom(ReportRequest request) {
        List<String> errors = new ArrayList<>();

        if (request.getStartDate() == null || request.getStartDate().isBlank()) {
            errors.add("Start Date - Month can NOT be empty...");
            errors.add("Start Date - Day can NOT be empty...");
            errors.add("Start Date - Year can NOT be empty...");
        } else {
            validateDateComponents(request.getStartDate(), "Start Date", errors);
        }

        if (request.getEndDate() == null || request.getEndDate().isBlank()) {
            errors.add("End Date - Month can NOT be empty...");
            errors.add("End Date - Day can NOT be empty...");
            errors.add("End Date - Year can NOT be empty...");
        } else {
            validateDateComponents(request.getEndDate(), "End Date", errors);
        }

        if (!errors.isEmpty()) {
            throw new ReportValidationException(errors);
        }

        LocalDate startDate = parseDate(request.getStartDate(), "Start Date");
        LocalDate endDate = parseDate(request.getEndDate(), "End Date");

        return new DateRange(startDate, endDate);
    }

    private void validateDateComponents(String dateStr, String prefix, List<String> errors) {
        String[] parts = dateStr.split("-");
        if (parts.length != 3) {
            errors.add(prefix + " - Not a valid date...");
            return;
        }

        String yearStr = parts[0];
        String monthStr = parts[1];
        String dayStr = parts[2];

        if (!isNumeric(yearStr)) {
            errors.add(prefix + " - Not a valid Year...");
        }

        if (!isNumeric(monthStr)) {
            errors.add(prefix + " - Not a valid Month...");
        } else {
            int month = Integer.parseInt(monthStr);
            if (month < 1 || month > 12) {
                errors.add(prefix + " - Not a valid Month...");
            }
        }

        if (!isNumeric(dayStr)) {
            errors.add(prefix + " - Not a valid Day...");
        } else {
            int day = Integer.parseInt(dayStr);
            if (day < 1 || day > 31) {
                errors.add(prefix + " - Not a valid Day...");
            }
        }
    }

    private LocalDate parseDate(String dateStr, String prefix) {
        try {
            return LocalDate.parse(dateStr, DATE_FORMAT);
        } catch (DateTimeParseException e) {
            throw new ReportValidationException(prefix + " - Not a valid date...");
        }
    }

    private boolean isNumeric(String str) {
        if (str == null || str.isBlank()) {
            return false;
        }
        for (char c : str.toCharArray()) {
            if (!Character.isDigit(c)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Represents a computed date range for a report.
     */
    public record DateRange(LocalDate startDate, LocalDate endDate) {
    }
}
