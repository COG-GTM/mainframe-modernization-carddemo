package com.carddemo.web.useradmin.dto;

/**
 * Error body returned when a user-administration operation is rejected — carries the verbatim
 * {@code COUSRxxC} message (see {@link com.carddemo.service.useradmin.UserAdminMessages}).
 *
 * @param message the verbatim operator message
 */
public record UserAdminErrorResponse(String message) {
}
