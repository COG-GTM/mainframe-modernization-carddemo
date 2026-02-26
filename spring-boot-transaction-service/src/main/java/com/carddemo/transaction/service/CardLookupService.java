package com.carddemo.transaction.service;

import com.carddemo.transaction.dto.CardLookupResponse;
import com.carddemo.transaction.entity.CardXref;
import com.carddemo.transaction.exception.ResourceNotFoundException;
import com.carddemo.transaction.exception.ValidationException;
import com.carddemo.transaction.repository.CardXrefRepository;
import org.springframework.stereotype.Service;

/**
 * Service for card/account cross-reference lookups.
 * Replaces the VALIDATE-INPUT-KEY-FIELDS paragraph in COTRN02C
 * and the associated READ-CXACAIX-FILE / READ-CCXREF-FILE paragraphs.
 *
 * COBOL flow:
 * - If Account ID provided: READ CXACAIX (alternate index) to get Card Number
 * - If Card Number provided: READ CCXREF (primary key) to get Account ID
 * - If neither: error "Account or Card Number must be entered"
 */
@Service
public class CardLookupService {

    private final CardXrefRepository cardXrefRepository;

    public CardLookupService(CardXrefRepository cardXrefRepository) {
        this.cardXrefRepository = cardXrefRepository;
    }

    /**
     * Resolves an account ID or card number to the full cross-reference record.
     *
     * @param accountId optional account ID (replaces ACTIDINI screen field)
     * @param cardNumber optional card number (replaces CARDNINI screen field)
     * @return the resolved card number
     * @throws ValidationException if neither identifier is provided
     * @throws ResourceNotFoundException if the identifier is not found
     */
    public String resolveCardNumber(String accountId, String cardNumber) {
        if (accountId != null && !accountId.isBlank()) {
            // Account-based lookup (replaces READ-CXACAIX-FILE)
            Long acctId = Long.parseLong(accountId);
            CardXref xref = cardXrefRepository.findByXrefAcctId(acctId)
                    .orElseThrow(() -> new ResourceNotFoundException("Account ID NOT found"));
            return xref.getXrefCardNum();
        } else if (cardNumber != null && !cardNumber.isBlank()) {
            // Card-based lookup (replaces READ-CCXREF-FILE)
            CardXref xref = cardXrefRepository.findByXrefCardNum(cardNumber)
                    .orElseThrow(() -> new ResourceNotFoundException("Card Number NOT found"));
            return xref.getXrefCardNum();
        }
        throw new ValidationException("Account or Card Number must be entered");
    }

    /**
     * Look up cross-reference by account ID.
     * Replaces: EXEC CICS READ DATASET('CXACAIX')
     */
    public CardLookupResponse lookupByAccountId(Long accountId) {
        CardXref xref = cardXrefRepository.findByXrefAcctId(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Account ID NOT found"));
        return new CardLookupResponse(
                xref.getXrefCardNum(),
                xref.getXrefAcctId(),
                xref.getXrefCustId()
        );
    }

    /**
     * Look up cross-reference by card number.
     * Replaces: EXEC CICS READ DATASET('CCXREF')
     */
    public CardLookupResponse lookupByCardNumber(String cardNumber) {
        CardXref xref = cardXrefRepository.findByXrefCardNum(cardNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Card Number NOT found"));
        return new CardLookupResponse(
                xref.getXrefCardNum(),
                xref.getXrefAcctId(),
                xref.getXrefCustId()
        );
    }
}
