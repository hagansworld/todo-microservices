package com.todo.notification_service.service;

import com.todo.notification_service.dto.TodoOverdueRequestDto;
import com.todo.notification_service.dto.TodoReminderRequestDto;
import com.todo.notification_service.entity.*;
import com.todo.notification_service.exception.EmailFailedException;
import com.todo.notification_service.repository.EmailLogRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@Slf4j
public class NotificationService {

    private final EmailTemplateService templateService;
    private final EmailSenderService senderService;
    private final EmailLogRepository emailLogRepository;
    private final VerificationCodeService verificationCodeService;
    private final String appUrl;

    // Explicit constructor so @Value is injected correctly alongside final fields
    public NotificationService(
            EmailTemplateService templateService,
            EmailSenderService senderService,
            EmailLogRepository emailLogRepository,
            VerificationCodeService verificationCodeService,
            @Value("${tasktide.app-url:http://localhost:6061}") String appUrl
    ) {
        this.templateService = templateService;
        this.senderService = senderService;
        this.emailLogRepository = emailLogRepository;
        this.verificationCodeService = verificationCodeService;
        this.appUrl = appUrl;
    }

    /* =========================
       EMAIL NOTIFICATIONS
       ========================= */

    // ── Verification ────────────────────────────────────────────────────────

    public String sendVerificationEmail(String email) {

        String code = verificationCodeService.generateCode();
        String html = templateService.loadVerificationTemplate(code);

        EmailLog emailLog = buildPendingLog(email, MessageType.VERIFICATION);

        try {
            senderService.sendHtmlEmail(email, "Verify Your TaskTide Account", html);
            markSent(emailLog);
        } catch (Exception e) {
            log.error("Failed to send verification email to {}: {}", email, e.getMessage());
            emailLog.setStatus(EmailStatus.FAILED);
            emailLogRepository.save(emailLog);
            throw new EmailFailedException("Failed to send verification email");
        }

        emailLogRepository.save(emailLog);
        return code;
    }

    // ── Welcome ──────────────────────────────────────────────────────────────

    public void sendWelcomeEmail(String email) {

        String html = templateService.loadWelcomeTemplate();
        EmailLog emailLog = buildPendingLog(email, MessageType.WELCOME);

        try {
            senderService.sendHtmlEmail(email, "Welcome to TaskTide 🎉", html);
            markSent(emailLog);
        } catch (Exception e) {
            log.error("Failed to send welcome email to {}: {}", email, e.getMessage());
            emailLog.setStatus(EmailStatus.FAILED);
            emailLogRepository.save(emailLog);
            throw new EmailFailedException("Failed to send welcome email");
        }

        emailLogRepository.save(emailLog);
    }

    // ── Password reset ───────────────────────────────────────────────────────

    public void sendPasswordResetEmail(String email, String resetToken) {

        String resetLink = appUrl + "/reset-password?token=" + resetToken;
        String html = templateService.loadResetPasswordTemplate(resetLink);

        EmailLog emailLog = buildPendingLog(email, MessageType.PASSWORD_RESET);

        try {
            senderService.sendHtmlEmail(email, "Reset Your Password", html);
            markSent(emailLog);
        } catch (Exception e) {
            log.error("Failed to send password reset email to {}: {}", email, e.getMessage());
            emailLog.setStatus(EmailStatus.FAILED);
            emailLogRepository.save(emailLog);
            throw new EmailFailedException("Failed to send password reset email");
        }

        emailLogRepository.save(emailLog);
    }

    // ── Todo reminder ────────────────────────────────────────────────────────

    public void sendTodoReminderEmail(TodoReminderRequestDto dto) {

        if (dto.getEmail() == null || dto.getEmail().isBlank()) {
            log.warn("Skipping TODO reminder email — missing address for user={}, task={}",
                    dto.getUserName(), dto.getTaskTitle());
            saveFailedEmailLog(null, MessageType.TODO_REMINDER);
            return;
        }

        log.info("Sending TODO reminder email to {} for task='{}'", dto.getEmail(), dto.getTaskTitle());
        log.info("DTO values — name='{}', dueDate='{}', priority='{}', appUrl='{}'",
                dto.getUserName(), dto.getDueDate(), dto.getPriority(), appUrl);

        EmailLog emailLog = buildPendingLog(dto.getEmail(), MessageType.TODO_REMINDER);

        try {
            String html = templateService.loadTodoReminderTemplate(
                    dto.getUserName(),
                    dto.getTaskTitle(),
                    dto.getDueDate(),
                    dto.getPriority(),
                    appUrl
            );

            senderService.sendHtmlEmail(
                    dto.getEmail(),
                    "Reminder: " + dto.getTaskTitle() + " is due soon",
                    html
            );

            markSent(emailLog);
            log.info("TODO reminder email sent successfully to {}", dto.getEmail());

        } catch (Exception e) {
            log.error("Failed to send TODO reminder email — FULL ERROR:", e);
            log.error("DTO dump: email={}, userName={}, taskTitle={}, dueDate={}, priority={}, appUrl={}",
                    dto.getEmail(), dto.getUserName(), dto.getTaskTitle(),
                    dto.getDueDate(), dto.getPriority(), appUrl);
            emailLog.setStatus(EmailStatus.FAILED);
        }

        emailLogRepository.save(emailLog);
    }

    // ── Todo overdue ─────────────────────────────────────────────────────────

    public void sendTodoOverdueEmail(TodoOverdueRequestDto dto) {

        if (dto.getEmail() == null || dto.getEmail().isBlank()) {
            log.warn("Skipping TODO overdue email — missing address for user={}, task={}",
                    dto.getUserName(), dto.getTaskTitle());
            saveFailedEmailLog(null, MessageType.TODO_OVERDUE);
            return;
        }

        log.info("Sending TODO overdue email to {} for task='{}'", dto.getEmail(), dto.getTaskTitle());
        log.info("DTO values — name='{}', dueDate='{}', appUrl='{}'",
                dto.getUserName(), dto.getDueDate(), appUrl);

        EmailLog emailLog = buildPendingLog(dto.getEmail(), MessageType.TODO_OVERDUE);

        try {
            String html = templateService.loadTodoOverdueTemplate(
                    dto.getUserName(),
                    dto.getTaskTitle(),
                    dto.getDueDate(),
                    appUrl
            );

            senderService.sendHtmlEmail(
                    dto.getEmail(),
                    "Task Overdue: " + dto.getTaskTitle(),
                    html
            );

            markSent(emailLog);
            log.info("TODO overdue email sent successfully to {}", dto.getEmail());

        } catch (Exception e) {
            log.error("Failed to send TODO overdue email — FULL ERROR:", e);
            log.error("DTO dump: email={}, userName={}, taskTitle={}, dueDate={}, appUrl={}",
                    dto.getEmail(), dto.getUserName(), dto.getTaskTitle(),
                    dto.getDueDate(), appUrl);
            emailLog.setStatus(EmailStatus.FAILED);
        }

        emailLogRepository.save(emailLog);
    }

    /* =========================
       PRIVATE HELPERS
       ========================= */

    private EmailLog buildPendingLog(String email, MessageType type) {
        EmailLog emailLog = new EmailLog();
        emailLog.setEmail(email);
        emailLog.setMessageType(type);
        emailLog.setStatus(EmailStatus.PENDING);
        return emailLog;
    }

    private void markSent(EmailLog emailLog) {
        emailLog.setStatus(EmailStatus.SENT);
        emailLog.setSentTime(LocalDateTime.now());
    }

    private void saveFailedEmailLog(String email, MessageType type) {
        EmailLog emailLog = new EmailLog();
        emailLog.setEmail(email);
        emailLog.setMessageType(type);
        emailLog.setStatus(EmailStatus.FAILED);
        emailLog.setSentTime(LocalDateTime.now());
        emailLogRepository.save(emailLog);
    }
}