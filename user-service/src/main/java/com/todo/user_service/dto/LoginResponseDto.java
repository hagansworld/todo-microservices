package com.todo.user_service.dto;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.todo.user_service.entity.UserRole;
import lombok.Data;

import java.util.UUID;

@JsonPropertyOrder({
        "id",
        "username",
        "email",
        "phoneNumber",
        "role",
        "token",
        "refreshToken"
})
@Data
public class LoginResponseDto {
    private UUID id;
    private String username;
    private String email;
    private UserRole role;
    private String token;
    private String refreshToken;
}
