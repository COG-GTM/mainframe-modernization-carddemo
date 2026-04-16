package com.carddemo.auth.dto;

public record UserDto(
        String userId,
        String firstName,
        String lastName,
        String userType
) {
}
