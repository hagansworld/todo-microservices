package com.todo_service.clients;

import com.todo_service.dto.ResponseDto;
import com.todo_service.dto.UserResponseDto;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;

import java.util.UUID;

@HttpExchange("/api/users")
public interface UserServiceClient {
    @GetExchange("/{id}")
    ResponseDto<UserResponseDto> getUserById(@PathVariable UUID id);
}



