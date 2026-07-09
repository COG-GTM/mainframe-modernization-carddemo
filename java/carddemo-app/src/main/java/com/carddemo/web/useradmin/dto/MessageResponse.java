package com.carddemo.web.useradmin.dto;

/**
 * Simple message envelope — used by {@code DELETE /api/admin/users/{userId}} to carry the
 * {@code COUSR03C} confirmation ({@code "User USER0004 has been deleted ..."}).
 *
 * @param message the operator confirmation message
 */
public record MessageResponse(String message) {
}
