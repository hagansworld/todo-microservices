package com.todo.user_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;


@Data
@AllArgsConstructor
public class NotificationPreferenceResponseDto {
    private boolean emailRemindersEnabled;
}