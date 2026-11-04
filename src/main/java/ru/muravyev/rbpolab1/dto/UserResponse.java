package ru.muravyev.rbpolab1.dto;

import ru.muravyev.rbpolab1.entity.UserRole;
import ru.muravyev.rbpolab1.entity.UserStatus;

import java.time.LocalDateTime;

public record UserResponse(
        Long id,
        String username,
        String name,
        String email,
        UserRole role,
        UserStatus status,
        LocalDateTime createdAt
) {
}