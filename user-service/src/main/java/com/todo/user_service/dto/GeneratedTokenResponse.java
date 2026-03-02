package com.todo.user_service.dto;

import lombok.Data;

@Data
public class GeneratedTokenResponse {
    private String authenticationToken;
    private String refreshToken;
}
