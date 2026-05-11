package com.todo_service.scheduler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.todo_service.clients.NotificationServiceClient;
import com.todo_service.clients.UserServiceClient;
import com.todo_service.dto.NotificationPreferenceResponseDto;
import com.todo_service.dto.ResponseDto;
import com.todo_service.dto.TodoOverdueRequestDto;
import com.todo_service.dto.TodoReminderRequestDto;
import com.todo_service.dto.UserResponseDto;
import com.todo_service.entity.Todo;
import com.todo_service.entity.TodoPriority;
import com.todo_service.entity.TodoReminder;
import com.todo_service.entity.TodoReminderType;
import com.todo_service.entity.TodoStatus;
import com.todo_service.repository.TodoReminderRepository;
import com.todo_service.repository.TodoRepository;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class TodoScheduler {

    private final TodoRepository todoRepository;
    private final TodoReminderRepository todoReminderRepository;
    private final UserServiceClient userServiceClient;
    private final NotificationServiceClient notificationClient;
    private final ObjectMapper objectMapper;

    private static final DateTimeFormatter DUE_DATE_FORMATTER =
            DateTimeFormatter.ofPattern("EEEE, MMM d yyyy 'at' h:mm a", Locale.ENGLISH);

    // ─── Reminders: every 5 minutes ───────────────────────────────────────────
    @Scheduled(cron = "0 */5 * * * *")
    public void sendUpcomingReminders() {
        LocalDateTime now = LocalDateTime.now();
        sendPriorityReminder(TodoPriority.HIGH,   now.plusHours(24), TodoReminderType.REMINDER_24H);
        sendPriorityReminder(TodoPriority.HIGH,   now.plusHours(12), TodoReminderType.REMINDER_12H);
        sendPriorityReminder(TodoPriority.MEDIUM, now.plusHours(24), TodoReminderType.REMINDER_24H);
    }

    // ─── Overdue: every minute ────────────────────────────────────────────────
    @Scheduled(cron = "0 * * * * *")
    public void markOverdueTodos() {

        List<Todo> overdueTodos = todoRepository.findByStatusInAndDueDateBefore(
                List.of(TodoStatus.TODO, TodoStatus.IN_PROGRESS),
                LocalDateTime.now()
        );

        log.info("Found {} overdue todos", overdueTodos.size());

        for (Todo todo : overdueTodos) {
            try {
                // Always mark as OVERDUE regardless of notification preference
                todo.setStatus(TodoStatus.OVERDUE);
                todoRepository.save(todo);

                // Dedup — skip if overdue email already sent
                if (todoReminderRepository.existsByTodoIdAndReminderType(
                        todo.getId(), TodoReminderType.OVERDUE)) {
                    log.debug("Overdue already sent for todo {}. Skipping.", todo.getId());
                    continue;
                }

                //  skip if reminders are DISABLED (! added)
                if (!isEmailRemindersEnabled(todo.getUserId())) {
                    log.info("Email reminders disabled for user {}. Skipping overdue email for todo {}.",
                            todo.getUserId(), todo.getId());
                    continue;
                }

                UserResponseDto user = fetchUser(todo);
                if (user == null) continue;

                TodoOverdueRequestDto notification = buildOverdueDto(todo, user);
                notificationClient.sendTodoOverdue(notification);

                saveReminderRecord(todo.getId(), TodoReminderType.OVERDUE);

                log.info("Overdue email sent for todo {}", todo.getId());

            } catch (Exception e) {
                log.error("Failed to process overdue todo {}. Will retry next run.", todo.getId(), e);
            }
        }
    }

    /* =========================
       UPCOMING REMINDERS
       ========================= */

    private void sendPriorityReminder(TodoPriority priority, LocalDateTime targetTime, TodoReminderType reminderType) {

        List<Todo> todos = todoRepository.findByStatusInAndDueDateBetween(
                List.of(TodoStatus.TODO, TodoStatus.IN_PROGRESS),
                targetTime.minusMinutes(5),
                targetTime.plusMinutes(5)
        );

        for (Todo todo : todos) {
            if (todo.getPriority() != priority) continue;

            try {
                // Dedup — skip if this reminder type already sent for this todo
                if (todoReminderRepository.existsByTodoIdAndReminderType(todo.getId(), reminderType)) {
                    log.debug("Reminder {} already sent for todo {}. Skipping.", reminderType, todo.getId());
                    continue;
                }

                //  FIXED: skip if reminders are DISABLED (! added)
                if (!isEmailRemindersEnabled(todo.getUserId())) {
                    log.info("Email reminders disabled for user {}. Skipping reminder {} for todo {}.",
                            todo.getUserId(), reminderType, todo.getId());
                    continue;
                }

                UserResponseDto user = fetchUser(todo);
                if (user == null) continue;

                TodoReminderRequestDto reminder = new TodoReminderRequestDto();
                reminder.setEmail(user.getEmail());
                reminder.setUserName(user.getFullName());
                reminder.setTaskTitle(todo.getTitle());
                reminder.setDueDate(formatDueDate(todo.getDueDate()));
                reminder.setPriority(todo.getPriority().name());

                notificationClient.sendTodoReminder(reminder);

                saveReminderRecord(todo.getId(), reminderType);

                log.info("Reminder {} sent for todo {}", reminderType, todo.getId());

            } catch (Exception e) {
                log.error("Failed to send reminder {} for todo {}. Skipping.", reminderType, todo.getId(), e);
            }
        }
    }

    /* =========================
       HELPERS
       ========================= */

    /**
     * Checks whether the user has email reminders enabled.
     * Defaults to TRUE if the preference cannot be fetched —
     * better to send than to silently skip.
     */
    private boolean isEmailRemindersEnabled(UUID userId) {
        try {
            ResponseDto<NotificationPreferenceResponseDto> response =
                    userServiceClient.getUserNotificationPreferences(userId);

            if (response == null || response.getData() == null) {
                log.warn("Could not fetch notification preference for user {}. Defaulting to enabled.", userId);
                return true;
            }

            NotificationPreferenceResponseDto pref =
                    objectMapper.convertValue(response.getData(), NotificationPreferenceResponseDto.class);

            return pref.isEmailRemindersEnabled();

        } catch (Exception e) {
            log.warn("Failed to fetch notification preference for user {}. Defaulting to enabled. Error: {}",
                    userId, e.getMessage());
            return true;
        }
    }

    private void saveReminderRecord(UUID todoId, TodoReminderType reminderType) {
        TodoReminder record = new TodoReminder();
        record.setTodoId(todoId);
        record.setReminderType(reminderType);
        todoReminderRepository.save(record);
    }

    private String formatDueDate(LocalDateTime dateTime) {
        if (dateTime == null) return "";
        return dateTime.format(DUE_DATE_FORMATTER);
    }

    private UserResponseDto fetchUser(Todo todo) {
        try {
            ResponseDto<UserResponseDto> response = userServiceClient.getUserById(todo.getUserId());

            if (response == null || response.getData() == null) {
                log.warn("User service returned null for todo {} (userId={})",
                        todo.getId(), todo.getUserId());
                return null;
            }

            UserResponseDto user = objectMapper.convertValue(response.getData(), UserResponseDto.class);

            if (user.getEmail() == null || user.getEmail().isBlank()) {
                log.warn("User {} has no email. Skipping notification for todo {}.",
                        todo.getUserId(), todo.getId());
                return null;
            }

            return user;

        } catch (Exception e) {
            log.error("Failed to fetch user for todo {} (userId={}): {}",
                    todo.getId(), todo.getUserId(), e.getMessage());
            return null;
        }
    }

    private static @NonNull TodoOverdueRequestDto buildOverdueDto(Todo todo, UserResponseDto user) {
        TodoOverdueRequestDto dto = new TodoOverdueRequestDto();
        dto.setEmail(user.getEmail());
        dto.setUserName(user.getFullName());
        dto.setTaskTitle(todo.getTitle());
        dto.setDueDate(todo.getDueDate() != null
                ? todo.getDueDate().format(DateTimeFormatter.ofPattern("EEEE, MMM d yyyy 'at' h:mm a", Locale.ENGLISH))
                : "");
        dto.setPriority(todo.getPriority() != null ? todo.getPriority().name() : "LOW");
        return dto;
    }
}


//package com.todo_service.scheduler;
//
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.todo_service.clients.NotificationServiceClient;
//import com.todo_service.clients.UserServiceClient;
//import com.todo_service.dto.ResponseDto;
//import com.todo_service.dto.TodoOverdueRequestDto;
//import com.todo_service.dto.TodoReminderRequestDto;
//import com.todo_service.dto.UserResponseDto;
//import com.todo_service.entity.Todo;
//import com.todo_service.entity.TodoPriority;
//import com.todo_service.entity.TodoReminder;
//import com.todo_service.entity.TodoReminderType;
//import com.todo_service.entity.TodoStatus;
//import com.todo_service.repository.TodoReminderRepository;
//import com.todo_service.repository.TodoRepository;
//import lombok.NonNull;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.scheduling.annotation.Scheduled;
//import org.springframework.stereotype.Component;
//
//import java.time.LocalDateTime;
//import java.time.format.DateTimeFormatter;
//import java.util.List;
//import java.util.Locale;
//
//@Component
//@RequiredArgsConstructor
//@Slf4j
//public class TodoScheduler {
//
//    private final TodoRepository todoRepository;
//    private final TodoReminderRepository todoReminderRepository;
//    private final UserServiceClient userServiceClient;
//    private final NotificationServiceClient notificationClient;
//    private final ObjectMapper objectMapper;
//
//    // e.g. "Wednesday, May 6 2026 at 8:00 AM"
//    private static final DateTimeFormatter DUE_DATE_FORMATTER =
//            DateTimeFormatter.ofPattern("EEEE, MMM d yyyy 'at' h:mm a", Locale.ENGLISH);
//
//    // ─── Reminders: every 5 minutes ───────────────────────────────────────────
//    @Scheduled(cron = "0 */5 * * * *")
//    public void sendUpcomingReminders() {
//        LocalDateTime now = LocalDateTime.now();
//        sendPriorityReminder(TodoPriority.HIGH,   now.plusHours(24), TodoReminderType.REMINDER_24H);
//        sendPriorityReminder(TodoPriority.HIGH,   now.plusHours(12), TodoReminderType.REMINDER_12H);
//        sendPriorityReminder(TodoPriority.MEDIUM, now.plusHours(24), TodoReminderType.REMINDER_24H);
//    }
//
//    // ─── Overdue: every minute ────────────────────────────────────────────────
//    @Scheduled(cron = "0 * * * * *")
//    public void markOverdueTodos() {
//
//        // Fixed: includes IN_PROGRESS — both statuses can go overdue
//        List<Todo> overdueTodos = todoRepository.findByStatusInAndDueDateBefore(
//                List.of(TodoStatus.TODO, TodoStatus.IN_PROGRESS),
//                LocalDateTime.now()
//        );
//
//        log.info("Found {} overdue todos", overdueTodos.size());
//
//        for (Todo todo : overdueTodos) {
//            try {
//                // Dedup — skip if overdue email already sent
//                if (todoReminderRepository.existsByTodoIdAndReminderType(todo.getId(), TodoReminderType.OVERDUE)) {
//                    log.debug("Overdue already sent for todo {}. Skipping.", todo.getId());
//                    continue;
//                }
//
//                UserResponseDto user = fetchUser(todo);
//                if (user == null) continue;
//
//                TodoOverdueRequestDto notification = buildOverdueDto(todo, user);
//                notificationClient.sendTodoOverdue(notification);
//
//                // Save reminder record first, then update status
//                saveReminderRecord(todo.getId(), TodoReminderType.OVERDUE);
//
//                todo.setStatus(TodoStatus.OVERDUE);
//                todoRepository.save(todo);
//
//                log.info("Overdue email sent for todo {}", todo.getId());
//
//            } catch (Exception e) {
//                log.error("Failed to process overdue todo {}. Will retry next run.", todo.getId(), e);
//            }
//        }
//    }
//
//    /* =========================
//       UPCOMING REMINDERS
//       ========================= */
//
//    private void sendPriorityReminder(TodoPriority priority, LocalDateTime targetTime, TodoReminderType reminderType) {
//
//        List<Todo> todos = todoRepository.findByStatusInAndDueDateBetween(
//                List.of(TodoStatus.TODO, TodoStatus.IN_PROGRESS),
//                targetTime.minusMinutes(5),
//                targetTime.plusMinutes(5)
//        );
//
//        for (Todo todo : todos) {
//            if (todo.getPriority() != priority) continue;
//
//            try {
//                // Dedup — skip if this reminder type already sent for this todo
//                if (todoReminderRepository.existsByTodoIdAndReminderType(todo.getId(), reminderType)) {
//                    log.debug("Reminder {} already sent for todo {}. Skipping.", reminderType, todo.getId());
//                    continue;
//                }
//
//                UserResponseDto user = fetchUser(todo);
//                if (user == null) continue;
//
//                TodoReminderRequestDto reminder = new TodoReminderRequestDto();
//                reminder.setEmail(user.getEmail());
//                reminder.setUserName(user.getFullName());
//                reminder.setTaskTitle(todo.getTitle());
//                reminder.setDueDate(formatDueDate(todo.getDueDate()));
//                reminder.setPriority(todo.getPriority().name());
//
//                notificationClient.sendTodoReminder(reminder);
//
//                // Record that this reminder was sent
//                saveReminderRecord(todo.getId(), reminderType);
//
//                log.info("Reminder {} sent for todo {}", reminderType, todo.getId());
//
//            } catch (Exception e) {
//                log.error("Failed to send reminder {} for todo {}. Skipping.", reminderType, todo.getId(), e);
//            }
//        }
//    }
//
//    /* =========================
//       HELPERS
//       ========================= */
//
//    private void saveReminderRecord(java.util.UUID todoId, TodoReminderType reminderType) {
//        TodoReminder record = new TodoReminder();
//        record.setTodoId(todoId);
//        record.setReminderType(reminderType);
//        todoReminderRepository.save(record);
//    }
//
//    private String formatDueDate(LocalDateTime dateTime) {
//        if (dateTime == null) return "";
//        return dateTime.format(DUE_DATE_FORMATTER);
//    }
//
//    /**
//     * Fetches a user by ID and safely converts the response data to UserResponseDto.
//     * Uses ObjectMapper.convertValue() to handle Jackson generic type erasure
//     * (ResponseDto<UserResponseDto> deserializes 'data' as LinkedHashMap at runtime).
//     */
//    private UserResponseDto fetchUser(Todo todo) {
//        try {
//            ResponseDto<UserResponseDto> response = userServiceClient.getUserById(todo.getUserId());
//
//            if (response == null || response.getData() == null) {
//                log.warn("User service returned null for todo {} (userId={})",
//                        todo.getId(), todo.getUserId());
//                return null;
//            }
//
//            UserResponseDto user = objectMapper.convertValue(response.getData(), UserResponseDto.class);
//
//            if (user.getEmail() == null || user.getEmail().isBlank()) {
//                log.warn("User {} has no email. Skipping notification for todo {}.",
//                        todo.getUserId(), todo.getId());
//                return null;
//            }
//
//            return user;
//
//        } catch (Exception e) {
//            log.error("Failed to fetch user for todo {} (userId={}): {}",
//                    todo.getId(), todo.getUserId(), e.getMessage());
//            return null;
//        }
//    }
//
//    private static @NonNull TodoOverdueRequestDto buildOverdueDto(Todo todo, UserResponseDto user) {
//        TodoOverdueRequestDto dto = new TodoOverdueRequestDto();
//        dto.setEmail(user.getEmail());
//        dto.setUserName(user.getFullName());
//        dto.setTaskTitle(todo.getTitle());
//        dto.setDueDate(todo.getDueDate() != null
//                ? todo.getDueDate().format(DateTimeFormatter.ofPattern("EEEE, MMM d yyyy 'at' h:mm a", Locale.ENGLISH))
//                : "");
//        dto.setPriority(todo.getPriority() != null ? todo.getPriority().name() : "LOW");
//        return dto;
//    }
//}