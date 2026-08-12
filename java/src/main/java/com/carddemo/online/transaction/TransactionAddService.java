package com.carddemo.online.transaction;

import com.carddemo.model.entity.CardXref;
import com.carddemo.model.entity.Transaction;
import com.carddemo.online.transaction.dto.TransactionAddRequest;
import com.carddemo.online.transaction.dto.TransactionAddResponse;
import com.carddemo.repository.CardXrefRepository;
import com.carddemo.repository.TransactionRepository;
import com.carddemo.util.CobolUtils;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * COBOL program: COTRN02C — "Add a Transaction to TRANSACT file" (transaction CT02).
 *
 * <p>Files: TRANSACT (copybook CVTRA05Y), CXACAIX and CCXREF (copybook CVACT03Y). Screen: BMS
 * mapset COTRN02, map COTRN2A. Date validity is checked by CSUTLDTC on the mainframe.
 *
 * <p>The validation order, the exact message texts, the confirm-before-commit flow and the
 * "highest key on file plus one" transaction id generation are preserved: on the 3270 every
 * SEND-TRNADD-SCREEN is followed by a CICS RETURN, so the first failing check ends the interaction.
 */
@Service
public class TransactionAddService {

    static final String MSG_ACCT_NOT_NUMERIC = "Account ID must be Numeric...";
    static final String MSG_CARD_NOT_NUMERIC = "Card Number must be Numeric...";
    static final String MSG_KEY_REQUIRED = "Account or Card Number must be entered...";
    static final String MSG_ACCT_NOT_FOUND = "Account ID NOT found...";
    static final String MSG_CARD_NOT_FOUND = "Card Number NOT found...";
    static final String MSG_TYPE_EMPTY = "Type CD can NOT be empty...";
    static final String MSG_CATEGORY_EMPTY = "Category CD can NOT be empty...";
    static final String MSG_SOURCE_EMPTY = "Source can NOT be empty...";
    static final String MSG_DESCRIPTION_EMPTY = "Description can NOT be empty...";
    static final String MSG_AMOUNT_EMPTY = "Amount can NOT be empty...";
    static final String MSG_ORIG_DATE_EMPTY = "Orig Date can NOT be empty...";
    static final String MSG_PROC_DATE_EMPTY = "Proc Date can NOT be empty...";
    static final String MSG_MERCHANT_ID_EMPTY = "Merchant ID can NOT be empty...";
    static final String MSG_MERCHANT_NAME_EMPTY = "Merchant Name can NOT be empty...";
    static final String MSG_MERCHANT_CITY_EMPTY = "Merchant City can NOT be empty...";
    static final String MSG_MERCHANT_ZIP_EMPTY = "Merchant Zip can NOT be empty...";
    static final String MSG_TYPE_NOT_NUMERIC = "Type CD must be Numeric...";
    static final String MSG_CATEGORY_NOT_NUMERIC = "Category CD must be Numeric...";
    static final String MSG_AMOUNT_FORMAT = "Amount should be in format -99999999.99";
    static final String MSG_ORIG_DATE_FORMAT = "Orig Date should be in format YYYY-MM-DD";
    static final String MSG_PROC_DATE_FORMAT = "Proc Date should be in format YYYY-MM-DD";
    static final String MSG_ORIG_DATE_INVALID = "Orig Date - Not a valid date...";
    static final String MSG_PROC_DATE_INVALID = "Proc Date - Not a valid date...";
    static final String MSG_MERCHANT_ID_NOT_NUMERIC = "Merchant ID must be Numeric...";
    static final String MSG_CONFIRM = "Confirm to add this transaction...";
    static final String MSG_CONFIRM_INVALID = "Invalid value. Valid values are (Y/N)...";
    static final String MSG_TRAN_ID_EXISTS = "Tran ID already exist...";

    private final TransactionRepository transactions;
    private final TransactionBrowseRepository transactionBrowse;
    private final CardXrefRepository cardXrefs;

    public TransactionAddService(
            TransactionRepository transactions,
            TransactionBrowseRepository transactionBrowse,
            CardXrefRepository cardXrefs) {
        this.transactions = transactions;
        this.transactionBrowse = transactionBrowse;
        this.cardXrefs = cardXrefs;
    }

    /** PROCESS-ENTER-KEY. */
    @Transactional
    public TransactionAddResponse add(TransactionAddRequest request) {
        ResolvedKeys keys;
        try {
            keys = validateInputKeyFields(request);
            validateInputDataFields(request);
        } catch (ScreenMessage failure) {
            return TransactionAddResponse.builder()
                    .success(false)
                    .message(failure.getMessage())
                    .build();
        }

        String confirm = request.getConfirm() == null ? "" : request.getConfirm().trim();
        if (confirm.equalsIgnoreCase("Y")) {
            return addTransaction(request, keys);
        }
        if (confirm.isEmpty() || confirm.equalsIgnoreCase("N")) {
            return TransactionAddResponse.builder()
                    .success(false)
                    .message(MSG_CONFIRM)
                    .accountId(keys.accountId())
                    .cardNumber(keys.cardNumber())
                    .build();
        }
        return TransactionAddResponse.builder()
                .success(false)
                .message(MSG_CONFIRM_INVALID)
                .accountId(keys.accountId())
                .cardNumber(keys.cardNumber())
                .build();
    }

    /** VALIDATE-INPUT-KEY-FIELDS: the account id wins over the card number, as in the EVALUATE. */
    private ResolvedKeys validateInputKeyFields(TransactionAddRequest request) {
        String accountId = trimToEmpty(request.getAccountId());
        String cardNumber = trimToEmpty(request.getCardNumber());

        if (!accountId.isEmpty()) {
            if (!isNumeric(accountId)) {
                throw new ScreenMessage(MSG_ACCT_NOT_NUMERIC);
            }
            long numericAccountId = Long.parseLong(accountId);
            CardXref xref = cardXrefs
                    .findFirstByAccountId(numericAccountId)
                    .orElseThrow(() -> new ScreenMessage(MSG_ACCT_NOT_FOUND));
            return new ResolvedKeys(CobolUtils.padLeftZeros(numericAccountId, 11), xref.getCardNumber());
        }

        if (!cardNumber.isEmpty()) {
            if (!isNumeric(cardNumber)) {
                throw new ScreenMessage(MSG_CARD_NOT_NUMERIC);
            }
            String key = CobolUtils.padLeftZeros(cardNumber, 16);
            CardXref xref = cardXrefs.findById(key).orElseThrow(() -> new ScreenMessage(MSG_CARD_NOT_FOUND));
            return new ResolvedKeys(CobolUtils.padLeftZeros(xref.getAccountId(), 11), key);
        }

        throw new ScreenMessage(MSG_KEY_REQUIRED);
    }

    /** VALIDATE-INPUT-DATA-FIELDS. */
    private void validateInputDataFields(TransactionAddRequest request) {
        requireNotEmpty(request.getTypeCode(), MSG_TYPE_EMPTY);
        requireNotEmpty(request.getCategoryCode(), MSG_CATEGORY_EMPTY);
        requireNotEmpty(request.getSource(), MSG_SOURCE_EMPTY);
        requireNotEmpty(request.getDescription(), MSG_DESCRIPTION_EMPTY);
        requireNotEmpty(request.getAmount(), MSG_AMOUNT_EMPTY);
        requireNotEmpty(request.getOriginDate(), MSG_ORIG_DATE_EMPTY);
        requireNotEmpty(request.getProcessDate(), MSG_PROC_DATE_EMPTY);
        requireNotEmpty(request.getMerchantId(), MSG_MERCHANT_ID_EMPTY);
        requireNotEmpty(request.getMerchantName(), MSG_MERCHANT_NAME_EMPTY);
        requireNotEmpty(request.getMerchantCity(), MSG_MERCHANT_CITY_EMPTY);
        requireNotEmpty(request.getMerchantZip(), MSG_MERCHANT_ZIP_EMPTY);

        if (!isNumeric(trimToEmpty(request.getTypeCode()))) {
            throw new ScreenMessage(MSG_TYPE_NOT_NUMERIC);
        }
        if (!isNumeric(trimToEmpty(request.getCategoryCode()))) {
            throw new ScreenMessage(MSG_CATEGORY_NOT_NUMERIC);
        }

        if (!isEditedAmount(request.getAmount())) {
            throw new ScreenMessage(MSG_AMOUNT_FORMAT);
        }

        if (!isEditedDate(request.getOriginDate())) {
            throw new ScreenMessage(MSG_ORIG_DATE_FORMAT);
        }
        if (!isEditedDate(request.getProcessDate())) {
            throw new ScreenMessage(MSG_PROC_DATE_FORMAT);
        }

        if (!isRealDate(request.getOriginDate())) {
            throw new ScreenMessage(MSG_ORIG_DATE_INVALID);
        }
        if (!isRealDate(request.getProcessDate())) {
            throw new ScreenMessage(MSG_PROC_DATE_INVALID);
        }

        if (!isNumeric(trimToEmpty(request.getMerchantId()))) {
            throw new ScreenMessage(MSG_MERCHANT_ID_NOT_NUMERIC);
        }
    }

    /** ADD-TRANSACTION and WRITE-TRANSACT-FILE. */
    private TransactionAddResponse addTransaction(TransactionAddRequest request, ResolvedKeys keys) {
        String transactionId = nextTransactionId();
        if (transactions.existsById(transactionId)) {
            return TransactionAddResponse.builder()
                    .success(false)
                    .message(MSG_TRAN_ID_EXISTS)
                    .accountId(keys.accountId())
                    .cardNumber(keys.cardNumber())
                    .build();
        }

        Transaction transaction = Transaction.builder()
                .transactionId(transactionId)
                .typeCode(trimToEmpty(request.getTypeCode()))
                .categoryCode(Integer.parseInt(trimToEmpty(request.getCategoryCode())))
                .source(trimToEmpty(request.getSource()))
                .description(trimToEmpty(request.getDescription()))
                .amount(numvalC(request.getAmount()))
                .cardNumber(keys.cardNumber())
                .merchantId(Long.parseLong(trimToEmpty(request.getMerchantId())))
                .merchantName(trimToEmpty(request.getMerchantName()))
                .merchantCity(trimToEmpty(request.getMerchantCity()))
                .merchantZip(trimToEmpty(request.getMerchantZip()))
                .originTimestamp(trimToEmpty(request.getOriginDate()))
                .processTimestamp(trimToEmpty(request.getProcessDate()))
                .build();
        transactions.save(transaction);

        return TransactionAddResponse.builder()
                .success(true)
                .message("Transaction added successfully.  Your Tran ID is " + transactionId + ".")
                .transactionId(transactionId)
                .accountId(keys.accountId())
                .cardNumber(keys.cardNumber())
                .build();
    }

    /**
     * STARTBR on HIGH-VALUES + READPREV + 1. An empty file leaves TRAN-ID at zeros (READPREV
     * ENDFILE), so the first generated id is 1.
     */
    private String nextTransactionId() {
        long highest = transactionBrowse
                .findFirstByOrderByTransactionIdDesc()
                .map(transaction -> Long.parseLong(transaction.getTransactionId().trim()))
                .orElse(0L);
        return TransactionFormats.transactionId(highest + 1);
    }

    /** FUNCTION NUMVAL-C on the edited TRNAMT field. */
    static BigDecimal numvalC(String amount) {
        String value = trimToEmpty(amount).replace(",", "").replace("$", "");
        boolean negative = value.startsWith("-");
        if (negative || value.startsWith("+")) {
            value = value.substring(1);
        }
        BigDecimal parsed = new BigDecimal(value);
        return negative ? parsed.negate() : parsed;
    }

    /** TRNAMTI(1:1) sign, (2:8) numeric, (10:1) '.', (11:2) numeric. */
    private static boolean isEditedAmount(String amount) {
        String value = CobolUtils.padRight(amount == null ? "" : amount, 12);
        char sign = value.charAt(0);
        return (sign == '-' || sign == '+')
                && isNumeric(value.substring(1, 9))
                && value.charAt(9) == '.'
                && isNumeric(value.substring(10, 12));
    }

    /** (1:4) numeric, (5:1) '-', (6:2) numeric, (8:1) '-', (9:2) numeric. */
    private static boolean isEditedDate(String date) {
        String value = CobolUtils.padRight(date == null ? "" : date, 10);
        return isNumeric(value.substring(0, 4))
                && value.charAt(4) == '-'
                && isNumeric(value.substring(5, 7))
                && value.charAt(7) == '-'
                && isNumeric(value.substring(8, 10));
    }

    /** CALL 'CSUTLDTC' with format YYYY-MM-DD: severity 0000 means the date exists. */
    private static boolean isRealDate(String date) {
        try {
            LocalDate.parse(trimToEmpty(date));
            return true;
        } catch (DateTimeParseException e) {
            return false;
        }
    }

    private static void requireNotEmpty(String value, String message) {
        if (trimToEmpty(value).isEmpty()) {
            throw new ScreenMessage(message);
        }
    }

    private static String trimToEmpty(String value) {
        return value == null ? "" : value.trim();
    }

    private static boolean isNumeric(String value) {
        if (value.isEmpty()) {
            return false;
        }
        for (int i = 0; i < value.length(); i++) {
            if (!Character.isDigit(value.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    /** The account id / card number pair after the cross reference read. */
    private record ResolvedKeys(String accountId, String cardNumber) {}

    /** A validation failure that on the 3270 ends in SEND-TRNADD-SCREEN followed by CICS RETURN. */
    private static class ScreenMessage extends RuntimeException {
        ScreenMessage(String message) {
            super(message);
        }
    }
}
