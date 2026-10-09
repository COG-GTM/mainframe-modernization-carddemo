package com.carddemo.web.dto;

/** Error envelope carrying the message the BMS map used to display. */
public record ApiError(int status, String message) {
}
