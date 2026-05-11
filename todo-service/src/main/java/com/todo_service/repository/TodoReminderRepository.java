package com.todo_service.repository;

import com.todo_service.entity.TodoReminder;
import com.todo_service.entity.TodoReminderType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface TodoReminderRepository extends JpaRepository<TodoReminder, UUID> {

    boolean existsByTodoIdAndReminderType(UUID todoId, TodoReminderType reminderType);
}