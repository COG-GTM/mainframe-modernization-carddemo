package com.carddemo.web.dto;

/** COCRDUPC - the editable fields of the card update map. */
public record CardUpdateRequest(
        Long accountId,
        String embossedName,
        String expirationDate,
        String activeStatus) {
}
