package com.todo.user_service.dto;

import lombok.Data;


@Data
public class UpdateNotificationPreferenceRequestDto {
    private boolean emailRemindersEnabled;
}