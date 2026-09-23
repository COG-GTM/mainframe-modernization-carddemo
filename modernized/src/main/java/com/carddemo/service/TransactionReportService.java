package com.carddemo.service;

import com.carddemo.config.CardDemoProperties;
import com.carddemo.domain.Transaction;
import com.carddemo.domain.TransactionCategoryType;
import com.carddemo.domain.TransactionCategoryTypeId;
import com.carddemo.domain.TransactionType;
import com.carddemo.exception.BusinessRuleException;
import com.carddemo.repository.CardXrefRepository;
import com.carddemo.repository.TransactionCategoryTypeRepository;
import com.carddemo.repository.TransactionRepository;
import com.carddemo.repository.TransactionTypeRepository;
import com.carddemo.util.CobolDateValidator;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * CORPT00C (report request screen) and CBTRN03C (TRANREPT print program).
 *
 * <p>The COBOL screen submitted a JCL job through a TDQ; here the report is produced directly and
 * written to the configured output directory using the CVTRA07Y print layout.
 */
@Service
public class TransactionReportService {

    private final TransactionRepository transactions;
    private final CardXrefRepository xrefs;
    private final TransactionTypeRepository transactionTypes;
    private final TransactionCategoryTypeRepository categoryTypes;
    private final CardDemoProperties properties;

    public TransactionReportService(TransactionRepository transactions,
                                    CardXrefRepository xrefs,
                                    TransactionTypeRepository transactionTypes,
                                    TransactionCategoryTypeRepository categoryTypes,
                                    CardDemoProperties properties) {
        this.transactions = transactions;
        this.xrefs = xrefs;
        this.transactionTypes = transactionTypes;
        this.categoryTypes = categoryTypes;
        this.properties = properties;
    }

    /** Resolves the Monthly / Yearly / Custom options offered by CORPT00C into a date range. */
    public DateRange resolveRange(String reportType, String startDate, String endDate) {
        String type = reportType == null ? "" : reportType.trim().toUpperCase();
        LocalDate today = LocalDate.now();
        return switch (type) {
            case "MONTHLY" -> new DateRange(today.withDayOfMonth(1), today);
            case "YEARLY" -> new DateRange(today.withDayOfYear(1), today.withMonth(12).withDayOfMonth(31));
            case "CUSTOM" -> new DateRange(requireDate(startDate, "Start Date"), requireDate(endDate, "End Date"));
            default -> throw new BusinessRuleException("Please select a report type to print report...");
        };
    }

    @Transactional(readOnly = true)
    public Path generate(String reportType, String startDate, String endDate) {
        DateRange range = resolveRange(reportType, startDate, endDate);
        List<Transaction> selected = transactions
                .findByProcessingTimestampBetweenOrderByCardNumberAscIdAsc(
                        range.start().toString(), range.end() + "\uFFFF");

        StringBuilder report = new StringBuilder();
        report.append(String.format("%-80s%n", "START DATE: " + range.start() + "   END DATE: " + range.end()));
        report.append(String.format("%-16s %-16s %-11s %-2s %-4s %14s%n",
                "TRANSACTION ID", "CARD NUMBER", "ACCOUNT", "TY", "CAT", "AMOUNT"));

        BigDecimal pageTotal = BigDecimal.ZERO;
        String currentCard = null;
        BigDecimal accountTotal = BigDecimal.ZERO;
        BigDecimal grandTotal = BigDecimal.ZERO;

        for (Transaction transaction : selected) {
            Long accountId = xrefs.findById(transaction.getCardNumber())
                    .map(xref -> xref.getAccountId())
                    .orElse(null);
            if (currentCard != null && !currentCard.equals(transaction.getCardNumber())) {
                report.append(totalLine("ACCOUNT TOTAL", accountTotal));
                accountTotal = BigDecimal.ZERO;
            }
            currentCard = transaction.getCardNumber();
            BigDecimal amount = transaction.getAmount() == null ? BigDecimal.ZERO : transaction.getAmount();
            accountTotal = accountTotal.add(amount);
            pageTotal = pageTotal.add(amount);
            grandTotal = grandTotal.add(amount);
            report.append(String.format("%-16s %-16s %-11s %-2s %-4s %14s   %s / %s%n",
                    transaction.getId(),
                    transaction.getCardNumber(),
                    accountId == null ? "" : accountId,
                    transaction.getTypeCode(),
                    transaction.getCategoryCode(),
                    amount.toPlainString(),
                    typeDescription(transaction.getTypeCode()),
                    categoryDescription(transaction.getTypeCode(), transaction.getCategoryCode())));
        }
        if (currentCard != null) {
            report.append(totalLine("ACCOUNT TOTAL", accountTotal));
        }
        report.append(totalLine("PAGE TOTAL", pageTotal));
        report.append(totalLine("GRAND TOTAL", grandTotal));

        return write("transaction-report-" + range.start() + "-to-" + range.end() + ".txt", report.toString());
    }

    private String typeDescription(String typeCode) {
        return transactionTypes.findById(typeCode).map(TransactionType::getDescription).orElse("");
    }

    private String categoryDescription(String typeCode, Integer categoryCode) {
        return categoryTypes.findById(new TransactionCategoryTypeId(typeCode, categoryCode))
                .map(TransactionCategoryType::getDescription)
                .orElse("");
    }

    private static String totalLine(String label, BigDecimal amount) {
        return String.format("%-20s %14s%n", label, amount.toPlainString());
    }

    private Path write(String fileName, String content) {
        try {
            Path directory = Path.of(properties.getOutputDirectory());
            Files.createDirectories(directory);
            Path file = directory.resolve(fileName);
            Files.writeString(file, content);
            return file;
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    private static LocalDate requireDate(String value, String label) {
        CobolDateValidator.Result result = CobolDateValidator.validate(value, "YYYY-MM-DD");
        if (!result.isValid()) {
            throw new BusinessRuleException(label + " - Not a valid date...");
        }
        return result.date();
    }

    /** Start and end date of a report run. */
    public record DateRange(LocalDate start, LocalDate end) {
    }
}
