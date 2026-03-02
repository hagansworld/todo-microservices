package com.todo_service.dto;

import com.todo_service.entity.TodoPriority;
import com.todo_service.entity.TodoStatus;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UpdateTodoRequestDto {
    private String title;
    private String description;
    private String category;
    private TodoPriority priority;
    private LocalDateTime dueDate;
    private TodoStatus status;
}
