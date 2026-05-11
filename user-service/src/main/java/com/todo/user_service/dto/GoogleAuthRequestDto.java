package com.todo.user_service.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class GoogleAuthRequestDto {

    @NotBlank(message = "Google ID token is required")
    private String idToken;
}