package com.todo.user_service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class VerifyEmailRequestDto {

    @NotNull(message="User is required")
    private UUID userId;

    @NotBlank(message = "Verification code is required")
    private String verificationCode;
}
