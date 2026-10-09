package com.carddemo.service;

import com.carddemo.domain.CardXref;
import com.carddemo.domain.Transaction;
import com.carddemo.exception.BusinessRuleException;
import com.carddemo.exception.RecordNotFoundException;
import com.carddemo.repository.CardXrefRepository;
import com.carddemo.repository.TransactionRepository;
import com.carddemo.util.CobolDateValidator;
import com.carddemo.web.dto.PageResponse;
import com.carddemo.web.dto.TransactionAddRequest;
import com.carddemo.web.dto.TransactionView;
import java.math.BigDecimal;
import java.util.regex.Pattern;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** COTRN00C (transaction list), COTRN01C (transaction view) and COTRN02C (transaction add). */
@Service
public class TransactionService {

    /** COTRN00C displayed ten rows per screen. */
    public static final int DEFAULT_PAGE_SIZE = 10;

    private static final Pattern TYPE_CODE = Pattern.compile("\\d{1,2}");
    private static final Pattern AMOUNT = Pattern.compile("[-+]\\d{1,8}\\.\\d{2}");
    private static final Pattern TIMESTAMP = Pattern.compile("\\d{4}-\\d{2}-\\d{2}( \\d{2}:\\d{2}:\\d{2}(\\.\\d+)?)?");

    private final TransactionRepository transactions;
    private final TransactionIdGenerator idGenerator;
    private final CardXrefRepository xrefs;

    public TransactionService(TransactionRepository transactions, CardXrefRepository xrefs,
                              TransactionIdGenerator idGenerator) {
        this.transactions = transactions;
        this.idGenerator = idGenerator;
        this.xrefs = xrefs;
    }

    @Transactional(readOnly = true)
    public PageResponse<TransactionView> list(int page, int size) {
        var result = transactions.findAllByOrderByIdAsc(PageRequest.of(page, size));
        if (result.isEmpty()) {
            throw new RecordNotFoundException("You are already at the top of the page...");
        }
        return PageResponse.of(result, TransactionService::toView);
    }

    @Transactional(readOnly = true)
    public TransactionView view(String transactionId) {
        String id = transactionId == null ? "" : transactionId.trim();
        if (id.isEmpty()) {
            throw new BusinessRuleException("Tran ID can NOT be empty...");
        }
        return transactions.findById(pad(id))
                .map(TransactionService::toView)
                .orElseThrow(() -> new RecordNotFoundException("Transaction ID NOT found..."));
    }

    @Transactional
    public TransactionView add(TransactionAddRequest request) {
        String cardNumber = resolveCardNumber(request);
        validate(request);

        Transaction transaction = new Transaction();
        transaction.setId(idGenerator.next());
        transaction.setTypeCode(String.format("%02d", Integer.parseInt(request.typeCode().trim())));
        transaction.setCategoryCode(request.categoryCode());
        transaction.setSource(request.source().trim());
        transaction.setDescription(request.description().trim());
        transaction.setAmount(new BigDecimal(request.amount().trim()));
        transaction.setCardNumber(cardNumber);
        transaction.setMerchantId(request.merchantId());
        transaction.setMerchantName(request.merchantName().trim());
        transaction.setMerchantCity(request.merchantCity().trim());
        transaction.setMerchantZip(request.merchantZip().trim());
        transaction.setOriginTimestamp(request.originTimestamp().trim());
        transaction.setProcessingTimestamp(request.processingTimestamp().trim());
        return toView(transactions.save(transaction));
    }

    /**
     * COTRN02C accepts either an account id (resolved through the CXACAIX alternate index) or a
     * card number (resolved through the primary xref key).
     */
    private String resolveCardNumber(TransactionAddRequest request) {
        boolean hasAccount = request.accountId() != null;
        boolean hasCard = request.cardNumber() != null && !request.cardNumber().isBlank();
        if (hasAccount == hasCard) {
            throw new BusinessRuleException("Account or Card Number must be entered...");
        }
        if (hasAccount) {
            AccountService.validateAccountId(request.accountId());
            CardXref xref = xrefs.findFirstByAccountIdOrderByCardNumber(request.accountId())
                    .orElseThrow(() -> new RecordNotFoundException("Account ID NOT found..."));
            return xref.getCardNumber();
        }
        String cardNumber = request.cardNumber().trim();
        if (!cardNumber.matches("\\d{16}")) {
            throw new BusinessRuleException("Card Number must be a 16 digit number...");
        }
        return xrefs.findById(cardNumber)
                .orElseThrow(() -> new RecordNotFoundException("Card Number NOT found..."))
                .getCardNumber();
    }

    private void validate(TransactionAddRequest request) {
        requireNotEmpty(request.typeCode(), "Type CD");
        if (request.categoryCode() == null) {
            throw new BusinessRuleException("Category CD can NOT be empty...");
        }
        requireNotEmpty(request.source(), "Source");
        requireNotEmpty(request.description(), "Description");
        requireNotEmpty(request.amount(), "Amount");
        requireNotEmpty(request.originTimestamp(), "Orig Date");
        requireNotEmpty(request.processingTimestamp(), "Proc Date");
        if (request.merchantId() == null) {
            throw new BusinessRuleException("Merchant ID can NOT be empty...");
        }
        requireNotEmpty(request.merchantName(), "Merchant Name");
        requireNotEmpty(request.merchantCity(), "Merchant City");
        requireNotEmpty(request.merchantZip(), "Merchant Zip");

        if (!TYPE_CODE.matcher(request.typeCode().trim()).matches()) {
            throw new BusinessRuleException("Type CD must be Numeric...");
        }
        if (!AMOUNT.matcher(request.amount().trim()).matches()) {
            throw new BusinessRuleException("Amount should be in format -99999999.99");
        }
        requireTimestamp(request.originTimestamp(), "Orig Date");
        requireTimestamp(request.processingTimestamp(), "Proc Date");
    }

    private void requireTimestamp(String value, String label) {
        String trimmed = value.trim();
        if (!TIMESTAMP.matcher(trimmed).matches()
                || !CobolDateValidator.isValidIsoDate(trimmed.substring(0, 10))) {
            throw new BusinessRuleException(label + " - Not a valid date...");
        }
    }

    private static void requireNotEmpty(String value, String label) {
        if (value == null || value.trim().isEmpty()) {
            throw new BusinessRuleException(label + " can NOT be empty...");
        }
    }


    private static String pad(String transactionId) {
        return transactionId.length() == 16 ? transactionId : String.format("%16s", transactionId).replace(' ', '0');
    }

    static TransactionView toView(Transaction transaction) {
        return new TransactionView(
                transaction.getId(),
                transaction.getTypeCode(),
                transaction.getCategoryCode(),
                transaction.getSource(),
                transaction.getDescription(),
                transaction.getAmount(),
                transaction.getMerchantId(),
                transaction.getMerchantName(),
                transaction.getMerchantCity(),
                transaction.getMerchantZip(),
                transaction.getCardNumber(),
                transaction.getOriginTimestamp(),
                transaction.getProcessingTimestamp());
    }
}
