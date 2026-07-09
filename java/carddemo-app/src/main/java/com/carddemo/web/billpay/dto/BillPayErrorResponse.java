package com.carddemo.web.billpay.dto;

/**
 * Error body for a rejected {@code POST /api/billpay} request, carrying the verbatim
 * {@code COBIL00C} {@code ERRMSGO} message.
 *
 * @param message the operator message that would have been shown on {@code COBIL0A}
 */
public record BillPayErrorResponse(String message) {
}
