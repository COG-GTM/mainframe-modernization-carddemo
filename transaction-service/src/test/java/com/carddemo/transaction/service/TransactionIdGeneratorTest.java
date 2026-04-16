package com.carddemo.transaction.service;

import com.carddemo.transaction.entity.Transaction;
import com.carddemo.transaction.repository.TransactionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

/**
 * Tests for sequential transaction ID generation.
 * Verifies the COBOL READPREV/HIGH-VALUES -> add 1 pattern.
 */
@ExtendWith(MockitoExtension.class)
class TransactionIdGeneratorTest {

    @Mock
    private TransactionRepository transactionRepository;

    @InjectMocks
    private TransactionIdGenerator idGenerator;

    @Test
    void generateNextId_noExistingTransactions_returnsOne() {
        when(transactionRepository.findFirstByOrderByTranIdDesc()).thenReturn(Optional.empty());
        assertEquals("0000000000000001", idGenerator.generateNextId());
    }

    @Test
    void generateNextId_existingTransaction_incrementsById() {
        Transaction lastTran = new Transaction();
        lastTran.setTranId("0000000000000042");
        when(transactionRepository.findFirstByOrderByTranIdDesc()).thenReturn(Optional.of(lastTran));
        assertEquals("0000000000000043", idGenerator.generateNextId());
    }

    @Test
    void generateNextId_highId_incrementsCorrectly() {
        Transaction lastTran = new Transaction();
        lastTran.setTranId("0000000000099999");
        when(transactionRepository.findFirstByOrderByTranIdDesc()).thenReturn(Optional.of(lastTran));
        assertEquals("0000000000100000", idGenerator.generateNextId());
    }
}
