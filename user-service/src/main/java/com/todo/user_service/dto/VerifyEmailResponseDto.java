package com.todo.user_service.dto;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Data;

@JsonPropertyOrder({
        "verified",
        "message"
})
@Data
public class VerifyEmailResponseDto {
    private boolean verified;
    private  String message;
}
