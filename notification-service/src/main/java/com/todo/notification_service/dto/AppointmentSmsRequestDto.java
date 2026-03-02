package com.todo.notification_service.dto;

import lombok.Data;

@Data
public class AppointmentSmsRequestDto {
    private String phone;
    private String name;
    private String date;
    private String time;
}
