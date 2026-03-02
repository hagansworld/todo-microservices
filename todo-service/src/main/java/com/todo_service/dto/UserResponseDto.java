package com.todo_service.dto;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.todo_service.entity.UserRole;
import com.todo_service.entity.UserStatus;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@JsonPropertyOrder({
        "id",
        "fullName",
        "username",
        "email",
        "phoneNumber",
        "role",
        "status",
        "address",
        "createdAt",
        "updatedAt"
})
public class UserResponseDto {
    private UUID id;
    private String fullName;
    private String username;
    private String email;
    private String phoneNumber;
    private UserRole role;
    private AddressDto address;
    private UserStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

}
