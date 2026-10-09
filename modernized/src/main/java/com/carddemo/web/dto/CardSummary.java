package com.carddemo.web.dto;

/** COCRDLIC / COCRDSLC - card list row and card detail. */
public record CardSummary(
        String cardNumber,
        Long accountId,
        Integer cvvCode,
        String embossedName,
        String expirationDate,
        String activeStatus,
        long version) {
}
