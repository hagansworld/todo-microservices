package com.todo.user_service.dto;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Data;

@JsonPropertyOrder({
        "sent",
        "message"
})
@Data
public class ResendVerificationResponseDto {
    private boolean sent;
    private String message;
}
