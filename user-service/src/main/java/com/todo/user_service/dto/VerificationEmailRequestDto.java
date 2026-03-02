package com.todo.user_service.dto;

import lombok.Data;

@Data
public class VerificationEmailRequestDto {
    private String email;

    // Custom constructor to only accept 'email'
    public VerificationEmailRequestDto(String email) {
        this.email = email;
    }

}
