package com.carddemo.transaction.service;

import com.carddemo.transaction.dto.CardLookupResponse;
import com.carddemo.transaction.entity.CardXref;
import com.carddemo.transaction.exception.ResourceNotFoundException;
import com.carddemo.transaction.exception.ValidationException;
import com.carddemo.transaction.repository.CardXrefRepository;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Unit tests for CardLookupService.
 * Tests the card/account cross-reference lookups migrated from
 * COTRN02C's VALIDATE-INPUT-KEY-FIELDS, READ-CXACAIX-FILE,
 * and READ-CCXREF-FILE paragraphs.
 */
@ExtendWith(MockitoExtension.class)
class CardLookupServiceTest {

    @Mock
    private CardXrefRepository cardXrefRepository;

    @InjectMocks
    private CardLookupService cardLookupService;

    private CardXref sampleXref;

    @BeforeEach
    void setUp() {
        sampleXref = new CardXref();
        sampleXref.setXrefCardNum("4111111111111111");
        sampleXref.setXrefAcctId(12345678901L);
        sampleXref.setXrefCustId(100000001L);
    }

    /**
     * Tests card number resolution by account ID.
     * Replaces: READ-CXACAIX-FILE (VSAM alternate index lookup).
     */
    @Test
    void resolveCardNumber_byAccountId() {
        org.mockito.Mockito.when(cardXrefRepository.findByXrefAcctId(12345678901L))
                .thenReturn(Optional.of(sampleXref));

        String cardNumber = cardLookupService.resolveCardNumber("12345678901", null);

        assertEquals("4111111111111111", cardNumber);
    }

    /**
     * Tests card number resolution by card number.
     * Replaces: READ-CCXREF-FILE (VSAM primary key lookup).
     */
    @Test
    void resolveCardNumber_byCardNumber() {
        org.mockito.Mockito.when(cardXrefRepository.findByXrefCardNum("4111111111111111"))
                .thenReturn(Optional.of(sampleXref));

        String cardNumber = cardLookupService.resolveCardNumber(null, "4111111111111111");

        assertEquals("4111111111111111", cardNumber);
    }

    /**
     * Tests that providing neither account ID nor card number throws ValidationException.
     * Replaces: "Account or Card Number must be entered" error in COBOL.
     */
    @Test
    void resolveCardNumber_neitherProvided() {
        assertThrows(ValidationException.class,
                () -> cardLookupService.resolveCardNumber(null, null));
    }

    /**
     * Tests that providing empty strings throws ValidationException.
     */
    @Test
    void resolveCardNumber_emptyStrings() {
        assertThrows(ValidationException.class,
                () -> cardLookupService.resolveCardNumber("", ""));
    }

    /**
     * Tests that a non-existent account ID throws ResourceNotFoundException.
     * Replaces: DFHRESP(NOTFND) in READ-CXACAIX-FILE -> "Account ID NOT found".
     */
    @Test
    void resolveCardNumber_accountNotFound() {
        org.mockito.Mockito.when(cardXrefRepository.findByXrefAcctId(99999999999L))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> cardLookupService.resolveCardNumber("99999999999", null));
    }

    /**
     * Tests that a non-existent card number throws ResourceNotFoundException.
     * Replaces: DFHRESP(NOTFND) in READ-CCXREF-FILE -> "Card Number NOT found".
     */
    @Test
    void resolveCardNumber_cardNotFound() {
        org.mockito.Mockito.when(cardXrefRepository.findByXrefCardNum("9999999999999999"))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> cardLookupService.resolveCardNumber(null, "9999999999999999"));
    }

    /**
     * Tests lookup by account ID returns full cross-reference data.
     */
    @Test
    void lookupByAccountId_success() {
        org.mockito.Mockito.when(cardXrefRepository.findByXrefAcctId(12345678901L))
                .thenReturn(Optional.of(sampleXref));

        CardLookupResponse response = cardLookupService.lookupByAccountId(12345678901L);

        assertNotNull(response);
        assertEquals("4111111111111111", response.getCardNumber());
        assertEquals(12345678901L, response.getAccountId());
        assertEquals(100000001L, response.getCustomerId());
    }

    /**
     * Tests lookup by card number returns full cross-reference data.
     */
    @Test
    void lookupByCardNumber_success() {
        org.mockito.Mockito.when(cardXrefRepository.findByXrefCardNum("4111111111111111"))
                .thenReturn(Optional.of(sampleXref));

        CardLookupResponse response = cardLookupService.lookupByCardNumber("4111111111111111");

        assertNotNull(response);
        assertEquals("4111111111111111", response.getCardNumber());
        assertEquals(12345678901L, response.getAccountId());
    }
}
