package com.todo.user_service.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class PasswordResetRequestDto {
    @Email(message = "The email address is not valid")
    @NotBlank(message = "Email is required")
    private String email;
}
