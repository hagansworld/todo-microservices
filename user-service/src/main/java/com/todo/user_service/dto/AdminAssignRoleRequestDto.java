package com.todo.user_service.dto;

import com.todo.user_service.entity.UserRole;
import lombok.Data;


@Data
public class AdminAssignRoleRequestDto {
//    private UUID userId;
    private UserRole role;
}
