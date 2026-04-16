package com.cardemo.batch.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for TransactionPostingResult model.
 */
class TransactionPostingResultTest {

    @Test
    @DisplayName("Posted result contains transaction and account data")
    void posted_containsTransactionAndAccount() {
        Transaction txn = new Transaction();
        txn.setId("TRAN0001");
        Account acct = new Account();
        acct.setAcctId(123L);
        TransactionCategoryBalance catBal = new TransactionCategoryBalance();

        TransactionPostingResult result = TransactionPostingResult.posted(txn, acct, catBal, true);

        assertTrue(result.isPosted());
        assertEquals("TRAN0001", result.getPostedTransaction().getId());
        assertEquals(123L, result.getUpdatedAccount().getAcctId());
        assertTrue(result.isNewCategoryBalance());
        assertNull(result.getRejectedTransaction());
    }

    @Test
    @DisplayName("Rejected result contains rejection data only")
    void rejected_containsRejectionOnly() {
        RejectedTransaction rejected = new RejectedTransaction();
        rejected.setTransactionId("TRAN0002");
        rejected.setFailReasonCode(100);

        TransactionPostingResult result = TransactionPostingResult.rejected(rejected);

        assertFalse(result.isPosted());
        assertNull(result.getPostedTransaction());
        assertNull(result.getUpdatedAccount());
        assertEquals("TRAN0002", result.getRejectedTransaction().getTransactionId());
    }
}
