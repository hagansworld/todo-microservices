package com.todo.notification_service.dto;

import lombok.Data;

@Data
public class TodoOverdueRequestDto {
    private String phone;
    private String userName;

    private String taskTitle;
    private String dueDate;
}
