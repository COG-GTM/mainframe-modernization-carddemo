package com.carddemo.service;

import com.carddemo.entity.AuthFraudRecord;
import com.carddemo.repository.AuthFraudRecordRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.Collections;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FraudServiceTest {
    @Mock private AuthFraudRecordRepository fraudRecordRepository;
    @InjectMocks private FraudService fraudService;

    @Test
    void markFraud_success() {
        when(fraudRecordRepository.save(any(AuthFraudRecord.class))).thenAnswer(i -> {
            AuthFraudRecord r = i.getArgument(0);
            r.setId(1L);
            return r;
        });
        AuthFraudRecord result = fraudService.markFraud("4111111111111111", 10000000001L, 100000001L, "5200");
        assertNotNull(result);
        assertEquals("Y", result.getAuthFraud());
        assertEquals("M", result.getMatchStatus());
        assertEquals("05", result.getAuthRespCode());
        assertEquals("5200", result.getAuthRespReason());
    }

    @Test
    void getFraudRecords_found() {
        AuthFraudRecord record = new AuthFraudRecord();
        record.setCardNum("4111111111111111");
        when(fraudRecordRepository.findByCardNum("4111111111111111")).thenReturn(List.of(record));
        List<AuthFraudRecord> results = fraudService.getFraudRecords("4111111111111111");
        assertEquals(1, results.size());
    }

    @Test
    void getFraudRecords_empty() {
        when(fraudRecordRepository.findByCardNum("0000")).thenReturn(Collections.emptyList());
        List<AuthFraudRecord> results = fraudService.getFraudRecords("0000");
        assertTrue(results.isEmpty());
    }
}
