package com.todo_service.mapper;

import com.todo_service.dto.CreateTodoRequestDto;
import com.todo_service.dto.TodoResponseDto;
import com.todo_service.entity.Todo;
import com.todo_service.entity.TodoStatus;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class TodoMapper {

    public Todo toTodo(CreateTodoRequestDto request, UUID userId, String username) {
        Todo todo = new Todo();
        todo.setUserId(userId);
        todo.setUsername(username);
        todo.setTitle(request.getTitle());
        todo.setDescription(request.getDescription());
        todo.setCategory(request.getCategory());
        todo.setPriority(request.getPriority());
        todo.setDueDate(request.getDueDate());
        todo.setStatus(TodoStatus.TODO); // always starts as TODO
        return todo;
    }

    public TodoResponseDto todoResponseDto(Todo todo) {
        TodoResponseDto response = new TodoResponseDto();
        response.setId(todo.getId());
        response.setUserId(todo.getUserId());
        response.setUsername(todo.getUsername());
        response.setTitle(todo.getTitle());
        response.setDescription(todo.getDescription());
        response.setCategory(todo.getCategory());
        response.setPriority(todo.getPriority());
        response.setStatus(todo.getStatus());
        response.setDueDate(todo.getDueDate());
        response.setCompletedAt(todo.getCompletedAt());
        response.setDeleted(todo.isDeleted());
        response.setCreatedAt(todo.getCreatedAt());
        response.setUpdatedAt(todo.getUpdatedAt());
        return response;
    }
}