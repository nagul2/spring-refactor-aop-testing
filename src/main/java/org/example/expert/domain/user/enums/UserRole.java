package org.example.expert.domain.user.enums;

import org.example.expert.domain.common.exception.InvalidRequestException;

import java.util.Arrays;

import static org.example.expert.domain.common.exception.ErrorMessage.INVALID_USER_ROLE;

public enum UserRole {
    ADMIN, USER;

    public static UserRole of(String role) {
        return Arrays.stream(UserRole.values())
                .filter(r -> r.name().equalsIgnoreCase(role))
                .findFirst()
                .orElseThrow(() -> new InvalidRequestException(INVALID_USER_ROLE.getMessage()));
    }
}
