package com.todo.notification_service.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class VerificationEmailRequestDto {
    @NotBlank(message = "email is required")
    private String email;
    @NotBlank(message = "Username is required")
    private String code;
}
