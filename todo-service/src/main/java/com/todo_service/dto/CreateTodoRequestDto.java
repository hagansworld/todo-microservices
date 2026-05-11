package com.todo_service.dto;

import com.todo_service.entity.TodoCategory;
import com.todo_service.entity.TodoPriority;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CreateTodoRequestDto {
    @NotBlank(message = "Title is required")
    private String title;

    private String description;

    @NotNull(message = "Category is required")
    private TodoCategory category;

    @NotNull(message = "Priority is required")
    private TodoPriority priority;

    private LocalDateTime dueDate;

}
