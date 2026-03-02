package com.todo.user_service.dto;

import lombok.Data;

@Data
public class CompleteUserRequestDto {
    private String fullName;
    private String phoneNumber;
    private AddressDto address;
}
