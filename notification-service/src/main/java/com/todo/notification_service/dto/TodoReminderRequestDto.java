package com.todo.notification_service.dto;

import lombok.Data;

@Data
public class TodoReminderRequestDto {

    private String email;        // replaces phone
    private String userName;
    private String taskTitle;
    private String dueDate;      // formatted string, e.g. "Monday, 12 May 2025 at 09:00"
    private String priority;     // HIGH | MEDIUM | LOW
}