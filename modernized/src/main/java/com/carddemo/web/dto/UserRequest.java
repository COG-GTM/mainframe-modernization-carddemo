package com.carddemo.web.dto;

/** COUSR01C / COUSR02C - user add and update. */
public record UserRequest(
        String userId,
        String firstName,
        String lastName,
        String password,
        String userType) {
}
