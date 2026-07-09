package com.carddemo.web.card.dto;

import java.util.List;

/**
 * Result of the card-list screen ({@code COCRDLIC}).
 *
 * <p>Mirrors the paged VSAM browse in {@code 9000-READ-FORWARD}/{@code 9100-READ-BACKWARDS}:
 * up to {@code WS-MAX-SCREEN-LINES} (7) rows keyed ascending by card number, with the
 * next/previous-page flags ({@code CA-NEXT-PAGE-EXISTS} / {@code CA-PREV-PAGE...}) surfaced
 * as booleans. {@code message} carries the screen message (e.g. informational or the
 * "NO RECORDS FOUND FOR THIS SEARCH CONDITION." condition).</p>
 *
 * @param page            zero-based page index (COBOL {@code WS-CA-SCREEN-NUM} is 1-based).
 * @param pageSize        rows per page — always {@code 7} ({@code WS-MAX-SCREEN-LINES}).
 * @param cards           the page rows, ascending by card number.
 * @param hasNextPage     whether a further page exists (look-ahead READNEXT succeeded).
 * @param hasPreviousPage whether an earlier page exists (page &gt; 0).
 * @param message         optional screen message.
 */
public record CardListResponse(
        int page,
        int pageSize,
        List<CardSummaryResponse> cards,
        boolean hasNextPage,
        boolean hasPreviousPage,
        String message) {
}
