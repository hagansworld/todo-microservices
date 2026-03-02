package com.todo_service.repository;

import com.todo_service.entity.Todo;
import com.todo_service.entity.TodoStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TodoRepository extends JpaRepository<Todo, UUID> {

    Optional<Todo> findTodoById(UUID id);

    Optional<Todo> findByIdAndUserId(UUID id, UUID userId);

    List<Todo> findByStatusAndDueDateBefore(TodoStatus status, LocalDateTime time);

    // Get all todos created by a specific user
    List<Todo> findAllByUserId(UUID userId);


    // find upcoming todos between now and a specific time window
    List<Todo> findByStatusInAndDueDateBetween(
            List<TodoStatus> statuses,
            LocalDateTime start,
            LocalDateTime end
    );
}
