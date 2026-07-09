package com.carddemo.web.card.dto;

/**
 * Result of a successful card update ({@code COCRDUPC} {@code 9200-WRITE-PROCESSING}).
 *
 * <p>{@code message} carries the COBOL confirmation ({@code CONFIRM-UPDATE-SUCCESS} —
 * "Changes committed to database"); {@code card} is the freshly-persisted detail so the
 * caller can re-display the record.</p>
 */
public record CardUpdateResponse(String message, CardDetailResponse card) {
}
