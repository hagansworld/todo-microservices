package com.todo.user_service.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CompleteUserRequestDto {
    @NotBlank(message = "Full name is required")
    private String fullName;

    private String phoneNumber;
}
