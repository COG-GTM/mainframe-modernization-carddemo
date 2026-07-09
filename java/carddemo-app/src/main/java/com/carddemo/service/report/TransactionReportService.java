package com.carddemo.service.report;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.carddemo.domain.CardXref;
import com.carddemo.domain.Transaction;
import com.carddemo.domain.TransactionCategory;
import com.carddemo.domain.TransactionCategoryId;
import com.carddemo.domain.TransactionType;
import com.carddemo.repository.CardXrefRepository;
import com.carddemo.repository.TransactionCategoryRepository;
import com.carddemo.repository.TransactionRepository;
import com.carddemo.repository.TransactionTypeRepository;
import com.carddemo.web.report.dto.AccountTotal;
import com.carddemo.web.report.dto.ReportRequest;
import com.carddemo.web.report.dto.ReportResponse;
import com.carddemo.web.report.dto.ReportType;
import com.carddemo.web.report.dto.TransactionReport;
import com.carddemo.web.report.dto.TransactionReportLine;

/**
 * Online transaction-report request ported from {@code app/cbl/CORPT00C.cbl}.
 *
 * <p>{@code CORPT00C} lets an operator request a Monthly / Yearly / Custom transaction report
 * and — after a {@code (Y/N)} confirmation — submits a batch job (JCL to the {@code JOBS} TDQ)
 * that runs {@code CBTRN03C} to print the {@code TRANREPT} dataset. This service reproduces the
 * report-type resolution, the {@code CORPT00C} date validations and messages, and the confirm
 * flow; on confirmation it <em>generates the report inline</em> from {@link Transaction} data
 * following the same filtering/grouping/totalling logic as {@code CBTRN03C} (see the CS-7
 * mapping doc). CS-14 will wire the equivalent batch scheduling.</p>
 */
@Service
public class TransactionReportService {

    /** WS-PAGE-SIZE PIC 9(03) VALUE 20 in {@code CBTRN03C}. */
    static final int PAGE_SIZE = 20;
    private static final DateTimeFormatter ISO = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final int MAX_MONTH = 12;
    private static final int MAX_DAY = 31;

    private final TransactionRepository transactionRepository;
    private final CardXrefRepository cardXrefRepository;
    private final TransactionTypeRepository transactionTypeRepository;
    private final TransactionCategoryRepository transactionCategoryRepository;

    public TransactionReportService(TransactionRepository transactionRepository,
            CardXrefRepository cardXrefRepository,
            TransactionTypeRepository transactionTypeRepository,
            TransactionCategoryRepository transactionCategoryRepository) {
        this.transactionRepository = transactionRepository;
        this.cardXrefRepository = cardXrefRepository;
        this.transactionTypeRepository = transactionTypeRepository;
        this.transactionCategoryRepository = transactionCategoryRepository;
    }

    /**
     * Perform one {@code PROCESS-ENTER-KEY} turn of {@code CORPT00C}.
     *
     * @throws ReportException for the COBOL error branches (no report type, invalid custom
     *         date field, invalid confirmation value).
     */
    @Transactional(readOnly = true)
    public ReportResponse requestReport(ReportRequest request) {
        ReportType type = request == null ? null : request.reportType();
        if (type == null) {
            throw new ReportException(ReportMessages.SELECT_REPORT_TYPE, HttpStatus.BAD_REQUEST);
        }

        DateRange range = resolveRange(type, request);
        String confirm = trimToNull(request.confirm());

        // SUBMIT-JOB-TO-INTRDR confirmation handling.
        if (confirm == null) {
            return ReportResponse.confirmationRequired(type, range.start(), range.end(),
                    ReportMessages.confirmToPrint(type));
        }
        if ("Y".equals(confirm) || "y".equals(confirm)) {
            TransactionReport report = generate(type, range);
            return ReportResponse.submitted(type, range.start(), range.end(),
                    ReportMessages.submittedForPrinting(type), report);
        }
        if ("N".equals(confirm) || "n".equals(confirm)) {
            return ReportResponse.declined(type, range.start(), range.end());
        }
        throw new ReportException(ReportMessages.invalidConfirm(confirm), HttpStatus.BAD_REQUEST);
    }

    // --- date range resolution -------------------------------------------------------------

    private DateRange resolveRange(ReportType type, ReportRequest request) {
        return switch (type) {
            case MONTHLY -> {
                LocalDate first = LocalDate.now().withDayOfMonth(1);
                yield new DateRange(first.format(ISO),
                        first.plusMonths(1).minusDays(1).format(ISO));
            }
            case YEARLY -> {
                int year = LocalDate.now().getYear();
                yield new DateRange(LocalDate.of(year, 1, 1).format(ISO),
                        LocalDate.of(year, 12, 31).format(ISO));
            }
            case CUSTOM -> customRange(request);
        };
    }

    private DateRange customRange(ReportRequest r) {
        // Empty-field checks (EVALUATE TRUE — first empty field wins).
        String sMonth = requireField(r.startMonth(), ReportMessages.START_MONTH_EMPTY);
        String sDay = requireField(r.startDay(), ReportMessages.START_DAY_EMPTY);
        String sYear = requireField(r.startYear(), ReportMessages.START_YEAR_EMPTY);
        String eMonth = requireField(r.endMonth(), ReportMessages.END_MONTH_EMPTY);
        String eDay = requireField(r.endDay(), ReportMessages.END_DAY_EMPTY);
        String eYear = requireField(r.endYear(), ReportMessages.END_YEAR_EMPTY);

        // Range/numeric checks (sequential IFs — first failure wins).
        int startMonth = monthOrDay(sMonth, MAX_MONTH, ReportMessages.START_MONTH_INVALID);
        int startDay = monthOrDay(sDay, MAX_DAY, ReportMessages.START_DAY_INVALID);
        int startYear = year(sYear, ReportMessages.START_YEAR_INVALID);
        int endMonth = monthOrDay(eMonth, MAX_MONTH, ReportMessages.END_MONTH_INVALID);
        int endDay = monthOrDay(eDay, MAX_DAY, ReportMessages.END_DAY_INVALID);
        int endYear = year(eYear, ReportMessages.END_YEAR_INVALID);

        // CSUTLDTC valid-date checks.
        LocalDate start = validDate(startYear, startMonth, startDay, ReportMessages.START_DATE_INVALID);
        LocalDate end = validDate(endYear, endMonth, endDay, ReportMessages.END_DATE_INVALID);
        return new DateRange(start.format(ISO), end.format(ISO));
    }

    private static String requireField(String value, String message) {
        String trimmed = trimToNull(value);
        if (trimmed == null) {
            throw new ReportException(message, HttpStatus.BAD_REQUEST);
        }
        return trimmed;
    }

    private static int monthOrDay(String value, int max, String message) {
        int parsed = parseNonNegative(value, message);
        if (parsed > max) {
            throw new ReportException(message, HttpStatus.BAD_REQUEST);
        }
        return parsed;
    }

    private static int year(String value, String message) {
        return parseNonNegative(value, message);
    }

    private static int parseNonNegative(String value, String message) {
        try {
            int parsed = Integer.parseInt(value.trim());
            if (parsed < 0) {
                throw new ReportException(message, HttpStatus.BAD_REQUEST);
            }
            return parsed;
        } catch (NumberFormatException ex) {
            throw new ReportException(message, HttpStatus.BAD_REQUEST);
        }
    }

    private static LocalDate validDate(int year, int month, int day, String message) {
        try {
            return LocalDate.of(year, month, day);
        } catch (DateTimeException ex) {
            throw new ReportException(message, HttpStatus.BAD_REQUEST);
        }
    }

    // --- report generation (mirrors CBTRN03C) ----------------------------------------------

    private TransactionReport generate(ReportType type, DateRange range) {
        List<Transaction> selected = new ArrayList<>();
        for (Transaction t : transactionRepository.findAll()) {
            String day = procDate(t);
            if (day != null && day.compareTo(range.start()) >= 0 && day.compareTo(range.end()) <= 0) {
                selected.add(t);
            }
        }
        // SORT FIELDS=(TRAN-CARD-NUM,A); stable secondary order by transaction id.
        selected.sort(Comparator
                .comparing((Transaction t) -> nullToEmpty(t.getTranCardNum()))
                .thenComparing(t -> nullToEmpty(t.getTranId())));

        Map<String, String> acctByCard = new HashMap<>();
        Map<String, String> typeDescByCode = new HashMap<>();
        Map<TransactionCategoryId, String> catDescById = new HashMap<>();

        List<TransactionReportLine> lines = new ArrayList<>();
        List<AccountTotal> accountTotals = new ArrayList<>();
        List<BigDecimal> pageTotals = new ArrayList<>();
        BigDecimal grandTotal = BigDecimal.ZERO.setScale(2);
        BigDecimal accountTotal = BigDecimal.ZERO.setScale(2);
        BigDecimal pageTotal = BigDecimal.ZERO.setScale(2);
        String currentCard = null;
        String currentAcct = null;
        int pageLines = 0;

        for (Transaction t : selected) {
            String cardNum = t.getTranCardNum();
            String accountId = acctByCard.computeIfAbsent(nullToEmpty(cardNum), this::lookupAccountId);
            if (currentCard != null && !nullToEmpty(cardNum).equals(currentCard)) {
                accountTotals.add(new AccountTotal(currentAcct, emptyToNull(currentCard), accountTotal));
                accountTotal = BigDecimal.ZERO.setScale(2);
            }
            currentCard = nullToEmpty(cardNum);
            currentAcct = accountId;

            // Page total every PAGE_SIZE detail lines (WS-LINE-COUNTER MOD WS-PAGE-SIZE).
            if (pageLines == PAGE_SIZE) {
                pageTotals.add(pageTotal);
                pageTotal = BigDecimal.ZERO.setScale(2);
                pageLines = 0;
            }

            BigDecimal amount = scale(t.getTranAmt());
            String typeDesc = typeDescByCode.computeIfAbsent(nullToEmpty(t.getTranTypeCd()),
                    this::lookupTypeDesc);
            String catDesc = catDescById.computeIfAbsent(
                    new TransactionCategoryId(t.getTranTypeCd(), t.getTranCatCd()),
                    this::lookupCategoryDesc);

            lines.add(new TransactionReportLine(t.getTranId(), accountId, t.getTranTypeCd(),
                    typeDesc, t.getTranCatCd(), catDesc, t.getTranSource(), amount));

            accountTotal = accountTotal.add(amount);
            pageTotal = pageTotal.add(amount);
            grandTotal = grandTotal.add(amount);
            pageLines++;
        }
        if (currentCard != null) {
            accountTotals.add(new AccountTotal(currentAcct, emptyToNull(currentCard), accountTotal));
        }
        if (pageLines > 0) {
            pageTotals.add(pageTotal);
        }

        return new TransactionReport(type.reportName(), range.start(), range.end(), lines,
                accountTotals, pageTotals, PAGE_SIZE, grandTotal, lines.size());
    }

    private String lookupAccountId(String cardNum) {
        if (cardNum == null || cardNum.isEmpty()) {
            return null;
        }
        return cardXrefRepository.findById(cardNum).map(CardXref::getXrefAcctId).orElse(null);
    }

    private String lookupTypeDesc(String typeCode) {
        if (typeCode == null || typeCode.isEmpty()) {
            return null;
        }
        return transactionTypeRepository.findById(typeCode)
                .map(TransactionType::getTranTypeDesc).orElse(null);
    }

    private String lookupCategoryDesc(TransactionCategoryId id) {
        if (id.getTranTypeCd() == null || id.getTranCatCd() == null) {
            return null;
        }
        return transactionCategoryRepository.findById(id)
                .map(TransactionCategory::getTranCatTypeDesc).orElse(null);
    }

    private static String procDate(Transaction t) {
        String ts = t.getTranProcTs();
        if (ts == null || ts.length() < 10) {
            return null;
        }
        return ts.substring(0, 10);
    }

    private static BigDecimal scale(BigDecimal value) {
        return (value == null ? BigDecimal.ZERO : value).setScale(2, RoundingMode.DOWN);
    }

    private static String nullToEmpty(String value) {
        return value == null ? "" : value;
    }

    private static String emptyToNull(String value) {
        return value == null || value.isEmpty() ? null : value;
    }

    private static String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    /** Resolved inclusive report date range, both bounds as {@code yyyy-MM-dd}. */
    private record DateRange(String start, String end) {
    }
}
