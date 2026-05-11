package com.todo_service.clients;

import com.todo_service.dto.TodoOverdueRequestDto;
import com.todo_service.dto.TodoReminderRequestDto;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;

@HttpExchange("/api/notifications")
public interface NotificationServiceClient {

    @PostExchange("/todo-reminder-email")   // ← updated
    void sendTodoReminder(@RequestBody TodoReminderRequestDto request);

    @PostExchange("/todo-overdue-email")    // ← updated
    void sendTodoOverdue(@RequestBody TodoOverdueRequestDto request);
}