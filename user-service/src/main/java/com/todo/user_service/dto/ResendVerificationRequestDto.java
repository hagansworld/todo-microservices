package com.todo.user_service.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ResendVerificationRequestDto {
    @NotBlank(message = "Email is required and must be valid")
    private String email;
}
