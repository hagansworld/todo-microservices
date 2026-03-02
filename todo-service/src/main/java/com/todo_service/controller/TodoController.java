package com.todo_service.controller;

import com.todo_service.dto.CreateTodoRequestDto;
import com.todo_service.dto.ResponseDto;
import com.todo_service.dto.UpdateTodoRequestDto;
import com.todo_service.response.ApiResponse;
import com.todo_service.service.TodoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Tag(
        name = "Todo Service",
        description = "Operations for managing user tasks / todos"
)
@RestController
@RequestMapping("/api/todos")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class TodoController {

    private final TodoService todoService;

    @Operation(
            summary = "Create a new todo",
            description = "Create a todo for the authenticated user"
    )
    @PostMapping("/create-todo")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ResponseDto> createTodo(
            @RequestBody CreateTodoRequestDto request
    ) {
        return ResponseEntity.status(201)
                .body(ApiResponse.buildResponse(
                        todoService.createTodo(request),
                        201,
                        "Todo created successfully"
                ));
    }

    @Operation(
            summary = "Get all todos",
            description = "Retrieve all todos belonging to the authenticated user"
    )
    @GetMapping
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ResponseDto> getTodos() {
        return ResponseEntity.ok(
                ApiResponse.buildResponse(
                        todoService.getAllTodos(),
                        200,
                        "Todos retrieved successfully"
                )
        );
    }

    @Operation(
            summary = "Get todo by ID",
            description = "Retrieve a specific todo by its UUID"
    )
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ResponseDto> getTodo(@PathVariable UUID id) {
        return ResponseEntity.ok(
                ApiResponse.buildResponse(
                        todoService.getTodo(id),
                        200,
                        "Todo retrieved successfully"
                )
        );
    }

    @Operation(
            summary = "Update todo",
            description = "Update an existing todo by its ID"
    )
    @PutMapping("/update-todo/{id}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ResponseDto> updateTodo(
            @PathVariable UUID id,
            @RequestBody UpdateTodoRequestDto request
    ) {
        return ResponseEntity.ok(
                ApiResponse.buildResponse(
                        todoService.updateTodo(id, request),
                        200,
                        "Todo updated successfully"
                )
        );
    }

    @Operation(
            summary = "Delete todo",
            description = "Soft delete a todo by its ID"
    )
    @DeleteMapping("/delete-todo/{id}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ResponseDto> deleteTodo(@PathVariable UUID id) {
        return ResponseEntity.ok(
                ApiResponse.buildResponse(
                        todoService.softDelete(id),
                        200,
                        "Todo deleted successfully"
                )
        );
    }
}
