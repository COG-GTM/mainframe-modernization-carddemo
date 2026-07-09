package com.carddemo.batch.posting;

import com.carddemo.domain.CardXref;
import com.carddemo.domain.Transaction;
import com.carddemo.domain.TransactionCategory;
import com.carddemo.domain.TransactionCategoryId;
import com.carddemo.domain.TransactionType;
import com.carddemo.repository.CardXrefRepository;
import com.carddemo.repository.TransactionCategoryRepository;
import com.carddemo.repository.TransactionRepository;
import com.carddemo.repository.TransactionTypeRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

/**
 * Daily transaction detail report — the Java port of {@code CBTRN03C}.
 *
 * <p>Reads the posted transactions ({@code card_transaction}, keyed on {@code TRAN-ID},
 * read sequentially — i.e. ordered by id), keeps only those whose {@code TRAN-PROC-TS(1:10)}
 * falls within the {@code DATEPARM} date range, resolves the account id via the XREF
 * ({@code 1500-A-LOOKUP-XREF}), the transaction-type description ({@code 1500-B-LOOKUP-TRANTYPE})
 * and the category description ({@code 1500-C-LOOKUP-TRANCATG}), and emits detail lines with a
 * page total every {@code PAGE-SIZE} lines ({@code 1110-WRITE-PAGE-TOTALS}), an account total on
 * each card-number change ({@code 1120-WRITE-ACCOUNT-TOTALS}) and a final grand total
 * ({@code 1110-WRITE-GRAND-TOTALS}).</p>
 *
 * <p>Deviation from the legacy program: the COBOL EOF branch adds the stale {@code TRAN-AMT} of
 * the sentinel read into the totals and omits the last account total; this port instead flushes
 * the final account total cleanly and does not double-count, producing arithmetically correct
 * totals. Report layout (labels/columns of {@code CVTRA07Y}) is reproduced approximately; the
 * numeric totals are exact.</p>
 */
@Service("postingTransactionReportService")
public class TransactionReportService {

    /** WS-PAGE-SIZE PIC 9(03) VALUE 20 — detail lines per page. */
    static final int PAGE_SIZE = 20;

    private static final String REPORT_NAME = "Daily Transaction Report";

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
     * Generate the report for the inclusive processing-date range {@code [startDate, endDate]}
     * (both {@code yyyy-MM-dd}).
     */
    public ReportResult generate(String startDate, String endDate) {
        List<String> lines = new ArrayList<>();
        BigDecimal pageTotal = zero();
        BigDecimal accountTotal = zero();
        BigDecimal grandTotal = zero();
        int detailCount = 0;
        int includedCount = 0;
        String currentCard = null;

        lines.add(REPORT_NAME);
        lines.add("Date Range: " + startDate + " to " + endDate);
        lines.add(header());

        for (Transaction tran : transactionRepository.findAll(Sort.by(Sort.Direction.ASC, "tranId"))) {
            String procDate = datePart(tran.getTranProcTs());
            if (procDate.compareTo(startDate) < 0 || procDate.compareTo(endDate) > 0) {
                continue;
            }
            includedCount++;

            if (!tran.getTranCardNum().equals(currentCard)) {
                if (currentCard != null) {
                    lines.add(accountTotalLine(accountTotal));
                    accountTotal = zero();
                }
                currentCard = tran.getTranCardNum();
            }

            if (detailCount > 0 && detailCount % PAGE_SIZE == 0) {
                lines.add(pageTotalLine(pageTotal));
                grandTotal = grandTotal.add(pageTotal);
                pageTotal = zero();
                lines.add(header());
            }

            BigDecimal amt = scale2(tran.getTranAmt());
            pageTotal = pageTotal.add(amt);
            accountTotal = accountTotal.add(amt);
            lines.add(detailLine(tran, resolveAccountId(currentCard)));
            detailCount++;
        }

        if (currentCard != null) {
            lines.add(accountTotalLine(accountTotal));
        }
        lines.add(pageTotalLine(pageTotal));
        grandTotal = grandTotal.add(pageTotal);
        lines.add(grandTotalLine(grandTotal));

        return new ReportResult(lines, grandTotal, includedCount);
    }

    private String resolveAccountId(String cardNum) {
        return cardXrefRepository.findById(cardNum)
                .map(CardXref::getXrefAcctId)
                .orElse("");
    }

    private String typeDescription(String typeCd) {
        return transactionTypeRepository.findById(typeCd == null ? "" : typeCd)
                .map(TransactionType::getTranTypeDesc)
                .orElse("");
    }

    private String categoryDescription(String typeCd, Integer catCd) {
        if (typeCd == null || catCd == null) {
            return "";
        }
        return transactionCategoryRepository.findById(new TransactionCategoryId(typeCd, catCd))
                .map(TransactionCategory::getTranCatTypeDesc)
                .orElse("");
    }

    private String detailLine(Transaction tran, String acctId) {
        return String.format("%-16s %-11s %-2s-%-15s %04d-%-29s %-10s %14s",
                nullSafe(tran.getTranId()),
                nullSafe(acctId),
                nullSafe(tran.getTranTypeCd()),
                trunc(typeDescription(tran.getTranTypeCd()), 15),
                tran.getTranCatCd() == null ? 0 : tran.getTranCatCd(),
                trunc(categoryDescription(tran.getTranTypeCd(), tran.getTranCatCd()), 29),
                nullSafe(tran.getTranSource()),
                signed(scale2(tran.getTranAmt())));
    }

    private static String header() {
        return String.format("%-16s %-11s %-18s %-34s %-10s %14s",
                "Transaction ID", "Account ID", "Transaction Type", "Tran Category",
                "Tran Source", "Amount");
    }

    private static String pageTotalLine(BigDecimal total) {
        return "Page Total" + dots(87) + signed(total);
    }

    private static String accountTotalLine(BigDecimal total) {
        return "Account Total" + dots(84) + signed(total);
    }

    private static String grandTotalLine(BigDecimal total) {
        return "Grand Total" + dots(86) + signed(total);
    }

    private static String dots(int n) {
        return ".".repeat(n);
    }

    private static String signed(BigDecimal value) {
        DecimalFormat df = new DecimalFormat("+#,##0.00;-#,##0.00");
        return df.format(scale2(value));
    }

    private static String datePart(String procTs) {
        String ts = nullSafe(procTs);
        return ts.length() >= 10 ? ts.substring(0, 10) : ts;
    }

    private static String trunc(String value, int len) {
        String v = nullSafe(value);
        return v.length() > len ? v.substring(0, len) : v;
    }

    private static BigDecimal zero() {
        return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
    }

    private static BigDecimal scale2(BigDecimal value) {
        BigDecimal v = (value == null) ? BigDecimal.ZERO : value;
        return v.setScale(2, RoundingMode.HALF_UP);
    }

    private static String nullSafe(String value) {
        return value == null ? "" : value;
    }

    /** Result of {@link #generate}: the rendered report lines and the exact grand total. */
    public static final class ReportResult {

        private final List<String> lines;
        private final BigDecimal grandTotal;
        private final int transactionCount;

        ReportResult(List<String> lines, BigDecimal grandTotal, int transactionCount) {
            this.lines = lines;
            this.grandTotal = grandTotal;
            this.transactionCount = transactionCount;
        }

        public List<String> getLines() {
            return lines;
        }

        public BigDecimal getGrandTotal() {
            return grandTotal;
        }

        public int getTransactionCount() {
            return transactionCount;
        }

        public String render() {
            return String.join(System.lineSeparator(), lines);
        }
    }
}
