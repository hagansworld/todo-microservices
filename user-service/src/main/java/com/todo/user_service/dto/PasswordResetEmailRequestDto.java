package com.todo.user_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PasswordResetEmailRequestDto {
    private String email;
    private String resetToken;
}
