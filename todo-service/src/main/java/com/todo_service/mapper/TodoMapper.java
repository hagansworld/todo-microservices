package com.todo_service.mapper;

import com.todo_service.dto.CreateTodoRequestDto;
import com.todo_service.dto.TodoResponseDto;
import com.todo_service.entity.Todo;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;

@Component
public class TodoMapper {

    // map createTodoRequest to TODO_DB
    @Transactional
    public Todo toTodo(CreateTodoRequestDto request, UUID userId, String username){
        Todo todo = new Todo();
        todo.setUserId(userId);
        todo.setUsername(username);
        todo.setTitle(request.getTitle());
        todo.setDescription(request.getDescription());
        todo.setCategory(request.getCategory());
        todo.setPriority(request.getPriority());
        todo.setDueDate(request.getDueDate());

        LocalDateTime now = LocalDateTime.now();
        todo.setCreatedAt(now);
        todo.setUpdatedAt(now);


        return  todo;
    }


    public TodoResponseDto todoResponseDto(Todo todo){
        TodoResponseDto response  = new TodoResponseDto();
        response.setId(todo.getId());
        response.setUserId(todo.getUserId());
        response.setUsername(todo.getUsername());
        response.setTitle(todo.getTitle());
        response.setDescription(todo.getDescription());
        response.setCategory(todo.getCategory());
        response.setPriority(todo.getPriority());
        response.setStatus(todo.getStatus());
        response.setDueDate(todo.getDueDate());
        response.setCreatedAt(todo.getCreatedAt());
        response.setUpdatedAt(todo.getUpdatedAt());

        return  response;
    }
}
