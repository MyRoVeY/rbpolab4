package ru.muravyev.rbpolab1.dto;

import ru.muravyev.rbpolab1.entity.UserRole;
import ru.muravyev.rbpolab1.entity.UserStatus;

public record UserRequest(
        String username,
        String password,
        String name,
        String email,
        UserRole role,
        UserStatus status
) {
}