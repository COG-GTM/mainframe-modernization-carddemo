package com.carddemo.service;

import com.carddemo.entity.AuthFraudRecord;
import com.carddemo.repository.AuthFraudRecordRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class FraudService {
    private final AuthFraudRecordRepository fraudRecordRepository;

    public FraudService(AuthFraudRecordRepository fraudRecordRepository) {
        this.fraudRecordRepository = fraudRecordRepository;
    }

    @Transactional
    public AuthFraudRecord markFraud(String cardNum, Long acctId, Long custId, String reason) {
        AuthFraudRecord record = new AuthFraudRecord();
        record.setCardNum(cardNum);
        record.setAuthTs(LocalDateTime.now());
        record.setAuthFraud("Y");
        record.setMatchStatus("M");
        record.setFraudRptDate(LocalDate.now());
        record.setAuthRespCode("05");
        record.setAuthRespReason(reason);
        record.setAcctId(acctId);
        record.setCustId(custId);
        return fraudRecordRepository.save(record);
    }

    public List<AuthFraudRecord> getFraudRecords(String cardNum) {
        return fraudRecordRepository.findByCardNum(cardNum);
    }
}
