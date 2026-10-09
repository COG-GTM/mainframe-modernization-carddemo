package com.carddemo.web.dto;

/** COBIL00C - bill payment. The COBOL screen required an explicit Y confirmation. */
public record BillPaymentRequest(Long accountId, String confirm) {
}
