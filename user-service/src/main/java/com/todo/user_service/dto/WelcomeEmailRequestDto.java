package com.todo.user_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class WelcomeEmailRequestDto {
    private String email;
}
