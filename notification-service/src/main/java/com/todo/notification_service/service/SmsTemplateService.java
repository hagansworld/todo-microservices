package com.todo.notification_service.service;

import com.todo.notification_service.exception.TemplateLoadException;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
@Service
public class SmsTemplateService {

    private static final String SMS_TEMPLATE_BASE_PATH = "templates/sms/";

    // Method to load the content of a template file
    private String loadTemplate(String templateName) {
        try {
            ClassPathResource resource =
                    new ClassPathResource(SMS_TEMPLATE_BASE_PATH + templateName);

            return new String(
                    resource.getInputStream().readAllBytes(),
                    StandardCharsets.UTF_8
            );
        } catch (IOException e) {
            throw new TemplateLoadException(
                    "Failed to load SMS template: " + templateName);
        }
    }

    // Appointment reminder
    public String appointmentReminder(
            String name,
            String date,
            String time
    ) {
        return loadTemplate("appointment-reminder.txt")
                .replace("{{NAME}}", name)
                .replace("{{DATE}}", date)
                .replace("{{TIME}}", time);
    }

    // Todo_reminder
    public String todoReminder(
            String name,
            String task,
            String date,
            String priority
    ) {
        return loadTemplate("todo-reminder.txt")
                .replace("{{NAME}}", name)
                .replace("{{TASK}}", task)
                .replace("{{DATE}}", date)
                .replace("{{PRIORITY}}", priority);
    }

    // Overdue_todo
    public String todoOverdue(
            String name,
            String task,
            String date
    ) {
        return loadTemplate("todo-overdue.txt")
                .replace("{{NAME}}", name)
                .replace("{{TASK}}", task)
                .replace("{{DATE}}", date);
    }
}
