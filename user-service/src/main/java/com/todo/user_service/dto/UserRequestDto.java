package com.todo.user_service.dto;

import com.todo.user_service.entity.UserRole;
import com.todo.user_service.entity.UserStatus;
import lombok.Data;

@Data
public class UserRequestDto {
    private String fullName;
    private String username;
    private String email;
    private String phoneNumber;
    private String password;
    private UserRole role;
    private AddressDto address;
    private UserStatus status;
}
