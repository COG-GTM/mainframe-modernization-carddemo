package com.carddemo.service.transaction;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.ResolverStyle;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Pattern;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.carddemo.domain.CardXref;
import com.carddemo.domain.Transaction;
import com.carddemo.repository.CardXrefRepository;
import com.carddemo.repository.TransactionRepository;
import com.carddemo.web.transaction.dto.TransactionAddRequest;
import com.carddemo.web.transaction.dto.TransactionAddResponse;
import com.carddemo.web.transaction.dto.TransactionDetailDto;
import com.carddemo.web.transaction.dto.TransactionListResponse;
import com.carddemo.web.transaction.dto.TransactionSummaryDto;

/**
 * Business logic for the online transaction screens — the Java port of {@code COTRN00C}
 * (list), {@code COTRN01C} (view) and {@code COTRN02C} (add). All amount handling is
 * {@link BigDecimal} (scale 2) and every fixed-width numeric identifier stays a
 * zero-padded {@link String}, per the project mapping rules.
 *
 * <p>The validation order and every rejection message are reproduced verbatim from the COBOL
 * so behaviour is identical to the 3270 screens (see {@link Messages}).</p>
 */
@Service
public class TransactionService {

    /** Rows per page on the {@code COTRN00} browse map (TRNID01..TRNID10). */
    public static final int PAGE_SIZE = 10;

    private static final int TRAN_ID_WIDTH = 16;
    private static final int ACCT_ID_WIDTH = 11;
    private static final int CARD_NUM_WIDTH = 16;
    private static final int MERCHANT_ID_WIDTH = 9;

    private static final Pattern DIGITS = Pattern.compile("\\d+");
    /** {@code S99999999.99}: sign + 8 digits + '.' + 2 digits (COTRN02C amount check). */
    private static final Pattern AMOUNT = Pattern.compile("[+-]\\d{8}\\.\\d{2}");
    /** {@code YYYY-MM-DD} shape (COTRN02C date position check). */
    private static final Pattern DATE = Pattern.compile("\\d{4}-\\d{2}-\\d{2}");
    private static final DateTimeFormatter STRICT_DATE =
            DateTimeFormatter.ofPattern("uuuu-MM-dd").withResolverStyle(ResolverStyle.STRICT);

    /** Verbatim {@code COTRN00C}/{@code COTRN01C}/{@code COTRN02C} screen messages. */
    public static final class Messages {
        // COTRN00C — list
        public static final String TRAN_ID_MUST_BE_NUMERIC = "Tran ID must be Numeric ...";
        public static final String INVALID_SELECTION = "Invalid selection. Valid value is S";
        public static final String ALREADY_AT_TOP = "You are already at the top of the page...";
        public static final String ALREADY_AT_BOTTOM = "You are already at the bottom of the page...";
        // COTRN01C — view
        public static final String TRAN_ID_EMPTY = "Tran ID can NOT be empty...";
        public static final String TRAN_ID_NOT_FOUND = "Transaction ID NOT found...";
        // COTRN02C — add: key fields
        public static final String ACCT_ID_NOT_NUMERIC = "Account ID must be Numeric...";
        public static final String ACCT_ID_NOT_FOUND = "Account ID NOT found...";
        public static final String CARD_NUM_NOT_NUMERIC = "Card Number must be Numeric...";
        public static final String CARD_NUM_NOT_FOUND = "Card Number NOT found...";
        public static final String ACCT_OR_CARD_REQUIRED = "Account or Card Number must be entered...";
        // COTRN02C — add: data fields (empty)
        public static final String TYPE_CD_EMPTY = "Type CD can NOT be empty...";
        public static final String CATEGORY_CD_EMPTY = "Category CD can NOT be empty...";
        public static final String SOURCE_EMPTY = "Source can NOT be empty...";
        public static final String DESCRIPTION_EMPTY = "Description can NOT be empty...";
        public static final String AMOUNT_EMPTY = "Amount can NOT be empty...";
        public static final String ORIG_DATE_EMPTY = "Orig Date can NOT be empty...";
        public static final String PROC_DATE_EMPTY = "Proc Date can NOT be empty...";
        public static final String MERCHANT_ID_EMPTY = "Merchant ID can NOT be empty...";
        public static final String MERCHANT_NAME_EMPTY = "Merchant Name can NOT be empty...";
        public static final String MERCHANT_CITY_EMPTY = "Merchant City can NOT be empty...";
        public static final String MERCHANT_ZIP_EMPTY = "Merchant Zip can NOT be empty...";
        // COTRN02C — add: data fields (format)
        public static final String TYPE_CD_NOT_NUMERIC = "Type CD must be Numeric...";
        public static final String CATEGORY_CD_NOT_NUMERIC = "Category CD must be Numeric...";
        public static final String AMOUNT_FORMAT = "Amount should be in format -99999999.99";
        public static final String ORIG_DATE_FORMAT = "Orig Date should be in format YYYY-MM-DD";
        public static final String PROC_DATE_FORMAT = "Proc Date should be in format YYYY-MM-DD";
        public static final String ORIG_DATE_INVALID = "Orig Date - Not a valid date...";
        public static final String PROC_DATE_INVALID = "Proc Date - Not a valid date...";
        public static final String MERCHANT_ID_NOT_NUMERIC = "Merchant ID must be Numeric...";
        // COTRN02C — confirm gate
        public static final String CONFIRM_TO_ADD = "Confirm to add this transaction...";
        public static final String INVALID_YN = "Invalid value. Valid values are (Y/N)...";

        private Messages() {
        }

        /** Success line built by the {@code COTRN02C} STRING (note the double space). */
        public static String addedSuccessfully(String tranId) {
            return "Transaction added successfully.  Your Tran ID is " + tranId + ".";
        }
    }

    private final TransactionRepository transactionRepository;
    private final CardXrefRepository cardXrefRepository;

    public TransactionService(TransactionRepository transactionRepository,
            CardXrefRepository cardXrefRepository) {
        this.transactionRepository = transactionRepository;
        this.cardXrefRepository = cardXrefRepository;
    }

    // --- COTRN00C : list -------------------------------------------------------------------

    /**
     * Browse a page of transactions ordered by {@code TRAN-ID}, starting at {@code startTranId}
     * (GTEQ), reproducing {@code COTRN00C}'s STARTBR/READNEXT window.
     *
     * @param startTranId optional browse start key; blank browses from the top. A non-blank,
     *                    non-numeric value is rejected with the COBOL "Tran ID must be Numeric".
     * @param pageSize    rows per page; non-positive falls back to {@link #PAGE_SIZE}.
     */
    @Transactional(readOnly = true)
    public TransactionListResponse list(String startTranId, int pageSize) {
        int size = pageSize > 0 ? pageSize : PAGE_SIZE;
        String start = trimToNull(startTranId);
        if (start != null && !DIGITS.matcher(start).matches()) {
            throw new TransactionValidationException(Messages.TRAN_ID_MUST_BE_NUMERIC);
        }
        String startKey = start == null ? "" : start;

        List<Transaction> ordered = transactionRepository.findAll(Sort.by(Sort.Direction.ASC, "tranId"));
        List<TransactionSummaryDto> rows = new ArrayList<>();
        boolean moreRecords = false;
        for (Transaction t : ordered) {
            if (t.getTranId().compareTo(startKey) < 0) {
                continue;
            }
            if (rows.size() == size) {
                moreRecords = true;
                break;
            }
            rows.add(toSummary(t));
        }

        String first = rows.isEmpty() ? null : rows.get(0).tranId();
        String last = rows.isEmpty() ? null : rows.get(rows.size() - 1).tranId();
        return new TransactionListResponse(rows, startKey, size, first, last, moreRecords, rows.size());
    }

    // --- COTRN01C : view -------------------------------------------------------------------

    /**
     * Read one transaction by id, reproducing {@code COTRN01C}: an empty id and a not-found
     * key raise the verbatim COBOL messages.
     */
    @Transactional(readOnly = true)
    public TransactionDetailDto view(String tranId) {
        String id = trimToNull(tranId);
        if (id == null) {
            throw new TransactionValidationException(Messages.TRAN_ID_EMPTY);
        }
        Transaction t = transactionRepository.findById(id)
                .orElseThrow(() -> new TransactionNotFoundException(Messages.TRAN_ID_NOT_FOUND));
        return toDetail(t);
    }

    // --- COTRN02C : add --------------------------------------------------------------------

    /**
     * Validate and add a transaction, reproducing {@code COTRN02C}'s
     * {@code VALIDATE-INPUT-KEY-FIELDS} → {@code VALIDATE-INPUT-DATA-FIELDS} → confirm gate →
     * id generation ({@code last + 1}) → write.
     */
    @Transactional
    public TransactionAddResponse add(TransactionAddRequest request) {
        String cardNum = validateKeyFields(request);
        validateDataFields(request);
        validateConfirm(request.confirm());

        String tranId = nextTranId();
        Transaction t = new Transaction();
        t.setTranId(tranId);
        t.setTranTypeCd(trimToNull(request.typeCd()));
        t.setTranCatCd(Integer.valueOf(trimToNull(request.categoryCd())));
        t.setTranSource(trimToNull(request.source()));
        t.setTranDesc(trimToNull(request.description()));
        t.setTranAmt(new BigDecimal(request.amount().trim()).setScale(2));
        t.setTranCardNum(cardNum);
        t.setTranMerchantId(padZeros(trimToNull(request.merchantId()), MERCHANT_ID_WIDTH));
        t.setTranMerchantName(trimToNull(request.merchantName()));
        t.setTranMerchantCity(trimToNull(request.merchantCity()));
        t.setTranMerchantZip(trimToNull(request.merchantZip()));
        t.setTranOrigTs(trimToNull(request.origDate()));
        t.setTranProcTs(trimToNull(request.procDate()));
        transactionRepository.save(t);

        return new TransactionAddResponse(tranId, Messages.addedSuccessfully(tranId));
    }

    // --- validation helpers ----------------------------------------------------------------

    /**
     * {@code VALIDATE-INPUT-KEY-FIELDS}: resolve the card number from either the account id
     * (via {@code CXACAIX}) or the card number (via {@code CCXREF}); reject when neither is
     * supplied, non-numeric, or has no cross-reference.
     *
     * @return the resolved 16-char card number to stamp onto the new transaction.
     */
    private String validateKeyFields(TransactionAddRequest request) {
        String acct = trimToNull(request.acctId());
        String card = trimToNull(request.cardNum());
        if (acct != null) {
            if (!DIGITS.matcher(acct).matches()) {
                throw new TransactionValidationException(Messages.ACCT_ID_NOT_NUMERIC);
            }
            String acctKey = padZeros(acct, ACCT_ID_WIDTH);
            List<CardXref> xrefs = cardXrefRepository.findByXrefAcctId(acctKey);
            if (xrefs.isEmpty()) {
                throw new TransactionValidationException(Messages.ACCT_ID_NOT_FOUND);
            }
            return xrefs.get(0).getXrefCardNum();
        }
        if (card != null) {
            if (!DIGITS.matcher(card).matches()) {
                throw new TransactionValidationException(Messages.CARD_NUM_NOT_NUMERIC);
            }
            String cardKey = padZeros(card, CARD_NUM_WIDTH);
            return cardXrefRepository.findById(cardKey)
                    .map(CardXref::getXrefCardNum)
                    .orElseThrow(() -> new TransactionValidationException(Messages.CARD_NUM_NOT_FOUND));
        }
        throw new TransactionValidationException(Messages.ACCT_OR_CARD_REQUIRED);
    }

    /**
     * {@code VALIDATE-INPUT-DATA-FIELDS}: empty checks first (in COBOL field order), then the
     * numeric/format/validity checks, matching the order of the {@code COTRN02C} EVALUATEs.
     */
    private void validateDataFields(TransactionAddRequest request) {
        requireNotEmpty(request.typeCd(), Messages.TYPE_CD_EMPTY);
        requireNotEmpty(request.categoryCd(), Messages.CATEGORY_CD_EMPTY);
        requireNotEmpty(request.source(), Messages.SOURCE_EMPTY);
        requireNotEmpty(request.description(), Messages.DESCRIPTION_EMPTY);
        requireNotEmpty(request.amount(), Messages.AMOUNT_EMPTY);
        requireNotEmpty(request.origDate(), Messages.ORIG_DATE_EMPTY);
        requireNotEmpty(request.procDate(), Messages.PROC_DATE_EMPTY);
        requireNotEmpty(request.merchantId(), Messages.MERCHANT_ID_EMPTY);
        requireNotEmpty(request.merchantName(), Messages.MERCHANT_NAME_EMPTY);
        requireNotEmpty(request.merchantCity(), Messages.MERCHANT_CITY_EMPTY);
        requireNotEmpty(request.merchantZip(), Messages.MERCHANT_ZIP_EMPTY);

        if (!DIGITS.matcher(request.typeCd().trim()).matches()) {
            throw new TransactionValidationException(Messages.TYPE_CD_NOT_NUMERIC);
        }
        if (!DIGITS.matcher(request.categoryCd().trim()).matches()) {
            throw new TransactionValidationException(Messages.CATEGORY_CD_NOT_NUMERIC);
        }
        if (!AMOUNT.matcher(request.amount().trim()).matches()) {
            throw new TransactionValidationException(Messages.AMOUNT_FORMAT);
        }
        if (!DATE.matcher(request.origDate().trim()).matches()) {
            throw new TransactionValidationException(Messages.ORIG_DATE_FORMAT);
        }
        if (!DATE.matcher(request.procDate().trim()).matches()) {
            throw new TransactionValidationException(Messages.PROC_DATE_FORMAT);
        }
        if (isInvalidDate(request.origDate().trim())) {
            throw new TransactionValidationException(Messages.ORIG_DATE_INVALID);
        }
        if (isInvalidDate(request.procDate().trim())) {
            throw new TransactionValidationException(Messages.PROC_DATE_INVALID);
        }
        if (!DIGITS.matcher(request.merchantId().trim()).matches()) {
            throw new TransactionValidationException(Messages.MERCHANT_ID_NOT_NUMERIC);
        }
    }

    /** {@code EVALUATE CONFIRMI}: only {@code Y}/{@code y} commits; N/blank vs other differ. */
    private void validateConfirm(String confirm) {
        String c = trimToNull(confirm);
        if (c != null && (c.equalsIgnoreCase("Y"))) {
            return;
        }
        if (c == null || c.equalsIgnoreCase("N")) {
            throw new TransactionValidationException(Messages.CONFIRM_TO_ADD);
        }
        throw new TransactionValidationException(Messages.INVALID_YN);
    }

    private static void requireNotEmpty(String value, String message) {
        if (trimToNull(value) == null) {
            throw new TransactionValidationException(message);
        }
    }

    private static boolean isInvalidDate(String value) {
        try {
            LocalDate.parse(value, STRICT_DATE);
            return false;
        } catch (RuntimeException ex) {
            return true;
        }
    }

    /**
     * Next {@code TRAN-ID}: {@code COTRN02C} browses to the highest key and adds one. With
     * fixed-width, zero-padded ids the natural string max is the numeric max.
     */
    private String nextTranId() {
        long max = transactionRepository.findAll().stream()
                .map(Transaction::getTranId)
                .filter(id -> id != null && DIGITS.matcher(id.trim()).matches())
                .max(Comparator.naturalOrder())
                .map(id -> Long.parseLong(id.trim()))
                .orElse(0L);
        return String.format("%0" + TRAN_ID_WIDTH + "d", max + 1);
    }

    // --- mapping ---------------------------------------------------------------------------

    private TransactionSummaryDto toSummary(Transaction t) {
        return new TransactionSummaryDto(t.getTranId(), formatListDate(t.getTranOrigTs()),
                t.getTranDesc(), t.getTranAmt());
    }

    private TransactionDetailDto toDetail(Transaction t) {
        return new TransactionDetailDto(t.getTranId(), t.getTranCardNum(), t.getTranTypeCd(),
                t.getTranCatCd(), t.getTranSource(), t.getTranAmt(), t.getTranDesc(),
                t.getTranOrigTs(), t.getTranProcTs(), t.getTranMerchantId(),
                t.getTranMerchantName(), t.getTranMerchantCity(), t.getTranMerchantZip());
    }

    /** {@code TDATExxO}: MM/DD/YY slice of a {@code YYYY-MM-DD...} origin timestamp. */
    private static String formatListDate(String origTs) {
        if (origTs == null) {
            return "";
        }
        String s = origTs.trim();
        if (s.length() >= 10 && DATE.matcher(s.substring(0, 10)).matches()) {
            return s.substring(5, 7) + "/" + s.substring(8, 10) + "/" + s.substring(2, 4);
        }
        return s;
    }

    private static String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    /** Zero-pad numeric digits to {@code width}, truncating high-order like a COBOL {@code PIC 9}. */
    private static String padZeros(String digits, int width) {
        if (digits == null) {
            return null;
        }
        String d = digits.trim();
        if (d.length() > width) {
            return d.substring(d.length() - width);
        }
        return "0".repeat(width - d.length()) + d;
    }
}
