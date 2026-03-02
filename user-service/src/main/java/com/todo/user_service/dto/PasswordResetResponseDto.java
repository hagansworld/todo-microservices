package com.todo.user_service.dto;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.AllArgsConstructor;
import lombok.Data;


@JsonPropertyOrder({
      "success",
        "message"
})
@Data
@AllArgsConstructor
public class PasswordResetResponseDto {
    private boolean success;
    private String message;
}
