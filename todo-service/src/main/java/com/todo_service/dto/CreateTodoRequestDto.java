package com.todo_service.dto;

import com.todo_service.entity.TodoPriority;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CreateTodoRequestDto {
    private String title;
    private String description;
    private String category;
    private TodoPriority priority;
    private LocalDateTime dueDate;


}
