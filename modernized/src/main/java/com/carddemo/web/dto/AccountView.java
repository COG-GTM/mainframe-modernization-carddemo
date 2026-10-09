package com.carddemo.web.dto;

import java.math.BigDecimal;

/** COACTVWC - account view: account master joined with its cross referenced customer. */
public record AccountView(
        Long accountId,
        String activeStatus,
        BigDecimal currentBalance,
        BigDecimal creditLimit,
        BigDecimal cashCreditLimit,
        String openDate,
        String expirationDate,
        String reissueDate,
        BigDecimal currentCycleCredit,
        BigDecimal currentCycleDebit,
        String groupId,
        CustomerView customer,
        long version) {
}
