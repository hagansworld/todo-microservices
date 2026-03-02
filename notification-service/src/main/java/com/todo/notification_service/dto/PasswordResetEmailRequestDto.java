package com.todo.notification_service.dto;

import lombok.Data;

@Data
public class PasswordResetEmailRequestDto {
    private String email;
    private String resetToken;
}
