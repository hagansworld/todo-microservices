package com.todo.user_service.dto;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Data;

import java.util.UUID;

@JsonPropertyOrder({
        "sent",
        "message"
})
@Data
public class ResendVerificationResponseDto {
    private boolean sent;
    private String message;
    private UUID userId;   // ← added so frontend can proceed to verify screen

}
