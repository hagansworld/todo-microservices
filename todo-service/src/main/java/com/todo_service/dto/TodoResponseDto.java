package com.todo_service.dto;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.todo_service.entity.TodoPriority;
import com.todo_service.entity.TodoStatus;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@JsonPropertyOrder({
        "id",
        "title",
        "description",
        "category",
        "priority",
        "status",
        "dueDate",
        "createdAt",
        "updatedAt"
})
public class TodoResponseDto {
    private UUID id;
    private UUID userId;
    private String username;
    private String title;
    private String description;
    private String category;
    private TodoPriority priority;
    private TodoStatus status;
    private LocalDateTime dueDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

}
