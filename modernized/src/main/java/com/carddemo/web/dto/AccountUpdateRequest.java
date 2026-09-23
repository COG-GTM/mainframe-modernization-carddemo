package com.carddemo.web.dto;

import java.math.BigDecimal;

/** COACTUPC - account update. All fields of the update map, account and customer alike. */
public record AccountUpdateRequest(
        String activeStatus,
        BigDecimal creditLimit,
        BigDecimal cashCreditLimit,
        BigDecimal currentBalance,
        BigDecimal currentCycleCredit,
        BigDecimal currentCycleDebit,
        String openDate,
        String expirationDate,
        String reissueDate,
        String groupId,
        CustomerView customer) {
}
