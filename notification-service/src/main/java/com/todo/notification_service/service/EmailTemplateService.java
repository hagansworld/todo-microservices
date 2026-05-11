package com.todo.notification_service.service;

import com.todo.notification_service.exception.TemplateLoadException;
import lombok.AllArgsConstructor;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

@Service
@AllArgsConstructor
public class EmailTemplateService {

    private String loadTemplate(String path) {
        try {
            ClassPathResource resource = new ClassPathResource(path);
            return new String(resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new TemplateLoadException("Failed to load email template: " + path);
        }
    }

    private String currentYear() {
        return String.valueOf(LocalDateTime.now().getYear());
    }

    // null-safe helper — prevents String.replace() NPE when a DTO field is null
    private static String safe(String value, String fallback) {
        return value != null ? value : fallback;
    }

    // ── Existing templates ──────────────────────────────────────────────────

    public String loadVerificationTemplate(String code) {
        return loadTemplate("templates/email/verification-email.html")
                .replace("{{CODE}}", safe(code, ""))
                .replace("{{YEAR}}", currentYear());
    }

    public String loadWelcomeTemplate() {
        return loadTemplate("templates/email/welcome-email.html")
                .replace("{{YEAR}}", currentYear());
    }

    public String loadResetPasswordTemplate(String resetLink) {
        return loadTemplate("templates/email/reset-password.html")
                .replace("{{RESET_LINK}}", safe(resetLink, "#"))
                .replace("{{YEAR}}", currentYear());
    }

    // ── Todo reminder email ─────────────────────────────────────────────────

    public String loadTodoReminderTemplate(
            String name,
            String taskTitle,
            String dueDate,
            String priority,
            String appUrl
    ) {
        String safePriority = safe(priority, "LOW");
        return loadTemplate("templates/email/todo-reminder-email.html")
                .replace("{{NAME}}",         safe(name, "there"))
                .replace("{{TASK_TITLE}}",   safe(taskTitle, ""))
                .replace("{{DUE_DATE}}",     safe(dueDate, ""))
                .replace("{{PRIORITY}}",     safePriority)
                .replace("{{PRIORITY_CLASS}}", safePriority.toLowerCase())
                .replace("{{APP_URL}}",      safe(appUrl, "#"))
                .replace("{{YEAR}}",         currentYear());
    }

    // ── Todo overdue email ──────────────────────────────────────────────────

    public String loadTodoOverdueTemplate(
            String name,
            String taskTitle,
            String dueDate,
            String appUrl
    ) {
        return loadTemplate("templates/email/todo-overdue-email.html")
                .replace("{{NAME}}",       safe(name, "there"))
                .replace("{{TASK_TITLE}}", safe(taskTitle, ""))
                .replace("{{DUE_DATE}}",   safe(dueDate, ""))
                .replace("{{APP_URL}}",    safe(appUrl, "#"))
                .replace("{{YEAR}}",       currentYear());
    }
}

//package com.todo.notification_service.service;
//
//import com.todo.notification_service.exception.TemplateLoadException;
//import lombok.AllArgsConstructor;
//import org.springframework.core.io.ClassPathResource;
//import org.springframework.stereotype.Service;
//
//import java.io.IOException;
//import java.nio.charset.StandardCharsets;
//import java.time.LocalDateTime;
//
//@Service
//@AllArgsConstructor
//public class EmailTemplateService {
//
//    // Load template from a given file path
//    private String loadTemplate(String path) {
//        try {
//            ClassPathResource resource = new ClassPathResource(path);
//            return new String(
//                    resource.getInputStream().readAllBytes(),
//                    StandardCharsets.UTF_8
//            );
//        } catch (IOException e) {
//            throw new TemplateLoadException("Failed to load email template: " + path);
//        }
//    }
//
//    private String currentYear() {
//        return String.valueOf(LocalDateTime.now().getYear());
//    }
//
//    // ── Existing templates ──────────────────────────────────────────────────
//
//    public String loadVerificationTemplate(String code) {
//        return loadTemplate("templates/email/verification-email.html")
//                .replace("{{CODE}}", code)
//                .replace("{{YEAR}}", currentYear());
//    }
//
//    public String loadWelcomeTemplate() {
//        return loadTemplate("templates/email/welcome-email.html")
//                .replace("{{YEAR}}", currentYear());
//    }
//
//    public String loadResetPasswordTemplate(String resetLink) {
//        return loadTemplate("templates/email/reset-password.html")
//                .replace("{{RESET_LINK}}", resetLink)
//                .replace("{{YEAR}}", currentYear());
//    }
//
//    // ── New: Todo reminder email ────────────────────────────────────────────
//
//    /**
//     * Loads the todo-reminder email template.
//     *
//     * @param name          Recipient's display name
//     * @param taskTitle     Title of the task
//     * @param dueDate       Human-readable due date string  (e.g. "Monday, 12 May 2025 at 09:00")
//     * @param priority      Priority label as stored on the entity  (HIGH / MEDIUM / LOW)
//     * @param appUrl        Deep-link / app URL to open TaskTide
//     */
//    public String loadTodoReminderTemplate(
//            String name,
//            String taskTitle,
//            String dueDate,
//            String priority,
//            String appUrl
//    ) {
//        return loadTemplate("templates/email/todo-reminder-email.html")
//                .replace("{{NAME}}", name)
//                .replace("{{TASK_TITLE}}", taskTitle)
//                .replace("{{DUE_DATE}}", dueDate)
//                .replace("{{PRIORITY}}", priority)
//                .replace("{{PRIORITY_CLASS}}", priority.toLowerCase())   // high / medium / low  → CSS class suffix
//                .replace("{{APP_URL}}", appUrl)
//                .replace("{{YEAR}}", currentYear());
//
//
//    }
//
//    // ── New: Todo overdue email ─────────────────────────────────────────────
//
//    /**
//     * Loads the todo-overdue email template.
//     *
//     * @param name          Recipient's display name
//     * @param taskTitle     Title of the overdue task
//     * @param dueDate       Human-readable original due date string
//     * @param appUrl        Deep-link / app URL to open TaskTide
//     */
//    public String loadTodoOverdueTemplate(
//            String name,
//            String taskTitle,
//            String dueDate,
//            String appUrl
//    ) {
//        return loadTemplate("templates/email/todo-overdue-email.html")
//                .replace("{{NAME}}", name)
//                .replace("{{TASK_TITLE}}", taskTitle)
//                .replace("{{DUE_DATE}}", dueDate)
//                .replace("{{APP_URL}}", appUrl)
//                .replace("{{YEAR}}", currentYear());
//
//
//    }
//}
//
//
//
////package com.todo.notification_service.service;
////
////import com.todo.notification_service.exception.TemplateLoadException;
////import lombok.AllArgsConstructor;
////import lombok.NoArgsConstructor;
////import org.springframework.core.io.ClassPathResource;
////import org.springframework.stereotype.Service;
////
////import java.io.IOException;
////import java.nio.charset.StandardCharsets;
////import java.time.LocalDateTime;
////
////@Service
////@AllArgsConstructor
////public class EmailTemplateService {
////
////    // load template from a given file path
////    private String loadTemplate(String path) {
////        try {
////            ClassPathResource resource = new ClassPathResource(path);
////            return new String(
////                    resource.getInputStream().readAllBytes(),
////                    StandardCharsets.UTF_8
////            );
////        } catch (IOException e) {
////            throw new TemplateLoadException("Failed to load email template");
////        }
////    }
////
//////  Method to load the verification email template and replace placeholders
////    public String loadVerificationTemplate(String code) {
////        return loadTemplate("templates/email/verification-email.html")
////                .replace("{{CODE}}", code)
////                .replace("{{YEAR}}", String.valueOf(LocalDateTime.now().getYear()));
////    }
////
////// Method to load the welcome email template and replace the year placeholder
////    public String loadWelcomeTemplate() {
////        return loadTemplate("templates/email/welcome-email.html")
////                .replace("{{YEAR}}", String.valueOf(LocalDateTime.now().getYear()));
////    }
////
////    // Method to load the password reset email template and replace the reset link and year
////    public String loadResetPasswordTemplate(String resetLink) {
////        return loadTemplate("templates/email/reset-password.html")
////                .replace("{{RESET_LINK}}", resetLink)
////                .replace("{{YEAR}}", String.valueOf(LocalDateTime.now().getYear()));
////    }
////
////}
