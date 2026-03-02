package com.todo.user_service.dto;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Data;

import java.util.UUID;

@JsonPropertyOrder({
        "id",
        "username",
        "email",
        "emailSent",
        "message"
})
@Data
public class RegisterResponseDto {
    private UUID id;
    private String username;
    private String email;
    private boolean emailSent;
    private String message;
}
