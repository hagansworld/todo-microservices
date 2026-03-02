package com.todo_service.scheduler;

import com.todo_service.clients.NotificationServiceClient;
import com.todo_service.clients.UserServiceClient;
import com.todo_service.dto.ResponseDto;
import com.todo_service.dto.TodoOverdueRequestDto;
import com.todo_service.dto.TodoReminderRequestDto;
import com.todo_service.dto.UserResponseDto;
import com.todo_service.entity.Todo;
import com.todo_service.entity.TodoPriority;
import com.todo_service.entity.TodoStatus;
import com.todo_service.repository.TodoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class TodoScheduler {

    private final TodoRepository todoRepository;
    private final UserServiceClient userServiceClient;
    private final NotificationServiceClient notificationClient;

    @Scheduled(fixedRate = 10_000) // 10 seconds (for testing)
    public void processTodos() {
        sendUpcomingReminders();
        markOverdueTodos();
    }

    /* =========================
       UPCOMING REMINDERS
       ========================= */

    private void sendUpcomingReminders() {
        LocalDateTime now = LocalDateTime.now();

        sendPriorityReminder(TodoPriority.HIGH, now.plusHours(24));
        sendPriorityReminder(TodoPriority.HIGH, now.plusHours(12));
        sendPriorityReminder(TodoPriority.MEDIUM, now.plusHours(24));
    }

    private void sendPriorityReminder(TodoPriority priority, LocalDateTime targetTime) {

        List<Todo> todos = todoRepository.findByStatusInAndDueDateBetween(
                List.of(TodoStatus.TODO, TodoStatus.IN_PROGRESS),
                targetTime.minusMinutes(1),
                targetTime.plusMinutes(1)
        );

        for (Todo todo : todos) {
            if (todo.getPriority() != priority) continue;

            try {
                ResponseDto<UserResponseDto> response =
                        userServiceClient.getUserById(todo.getUserId());

                if (response == null || response.getData() == null) {
                    log.warn(
                            "User service returned null for todo {} (userId={})",
                            todo.getId(),
                            todo.getUserId()
                    );
                    continue;
                }

                UserResponseDto user = response.getData();

                TodoReminderRequestDto reminder = new TodoReminderRequestDto();
                reminder.setPhone(user.getPhoneNumber());
                reminder.setUserName(user.getFullName());
                reminder.setTaskTitle(todo.getTitle());
                reminder.setDueDate(todo.getDueDate().toString());
                reminder.setPriority(todo.getPriority().name());

                notificationClient.sendTodoReminder(reminder);

                log.info("Reminder sent for todo {}", todo.getId());

            } catch (Exception e) {
                log.error(
                        "Failed to send reminder for todo {}. Skipping.",
                        todo.getId(),
                        e
                );
            }
        }
    }

    /* =========================
       OVERDUE HANDLING
       ========================= */

    private void markOverdueTodos() {

        List<Todo> overdueTodos = todoRepository
                .findByStatusAndDueDateBefore(
                        TodoStatus.TODO,
                        LocalDateTime.now()
                );

        log.info("Found {} overdue todos", overdueTodos.size());

        for (Todo todo : overdueTodos) {
            try {
                ResponseDto<UserResponseDto> response =
                        userServiceClient.getUserById(todo.getUserId());

                if (response == null || response.getData() == null) {
                    log.warn(
                            "User not found for todo {} (userId={}). Skipping overdue notification.",
                            todo.getId(),
                            todo.getUserId()
                    );
                    continue;
                }

                UserResponseDto user = response.getData();

                TodoOverdueRequestDto notification =
                        getTodoOverdueRequestDto(todo, user);

                notificationClient.sendTodoOverdue(notification);

                // mark overdue ONLY after successful notification
                todo.setStatus(TodoStatus.OVERDUE);
                todoRepository.save(todo);

                log.info("Overdue notification sent for todo {}", todo.getId());

            } catch (Exception e) {
                log.error(
                        "Failed to process overdue todo {}. Will retry next run.",
                        todo.getId(),
                        e
                );
            }
        }
    }

    /* =========================
       HELPER
       ========================= */

    private static @NonNull TodoOverdueRequestDto getTodoOverdueRequestDto(
            Todo todo,
            UserResponseDto user
    ) {
        TodoOverdueRequestDto notification = new TodoOverdueRequestDto();
        notification.setPhone(user.getPhoneNumber());
        notification.setUserName(user.getFullName());
        notification.setTaskTitle(todo.getTitle());
        notification.setDueDate(todo.getDueDate().toString());
        notification.setPriority(
                todo.getPriority() != null
                        ? todo.getPriority().name()
                        : "LOW"
        );
        return notification;
    }
}
