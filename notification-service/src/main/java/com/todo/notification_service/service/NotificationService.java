package com.todo.notification_service.service;

import com.todo.notification_service.dto.TodoOverdueRequestDto;
import com.todo.notification_service.dto.TodoReminderRequestDto;
import com.todo.notification_service.entity.*;
import com.todo.notification_service.exception.EmailFailedException;
import com.todo.notification_service.repository.EmailLogRepository;
import com.todo.notification_service.repository.SmsLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final EmailTemplateService templateService;
    private final EmailSenderService senderService;
    private final EmailLogRepository emailLogRepository;
    private final SmsTemplateService smsTemplateService;
    private final SmsSenderService smsSenderService;
    private final VerificationCodeService verificationCodeService;
    private final SmsLogRepository smsLogRepository;

    /* =========================
     EMAIL NOTIFICATIONS
     ========================= */

    // Method to send verification email
    public String sendVerificationEmail(String email) {

        //  Generate code
        String code = verificationCodeService.generateCode();

         // Build HTML content from template
        String html = templateService.loadVerificationTemplate(code);

        // Create a new email log entry for pending status
        EmailLog log = new EmailLog();
        log.setEmail(email);
        log.setMessageType(MessageType.VERIFICATION);
        log.setStatus(EmailStatus.PENDING);

        // Attempt to send the email
        try {
            senderService.sendHtmlEmail(
                    email,
                    "Verify Your TaskTide Account",
                    html
            );

            // If the email is sent successfully, update the log status to SENT
            log.setStatus(EmailStatus.SENT);
            log.setSentTime(LocalDateTime.now());

        } catch (Exception e) {
            // If an error occurs, update the log status to FAILED and throw a custom exception
            log.setStatus(EmailStatus.FAILED);

            emailLogRepository.save(log);

            // Throw a custom exception to indicate failure
            throw new EmailFailedException("Failed to send verification email");
        }

        emailLogRepository.save(log);

        // RETURN CODE (Auth service will save it)
        return code;
    }


    // Method to send Welcome email
    public void sendWelcomeEmail(String email) {

        String html = templateService.loadWelcomeTemplate();

        EmailLog log = new EmailLog();
        log.setEmail(email);
        log.setMessageType(MessageType.WELCOME);
        log.setStatus(EmailStatus.PENDING);

        try {
            senderService.sendHtmlEmail(
                    email,
                    "Welcome to TaskTide 🎉",
                    html
            );

            log.setStatus(EmailStatus.SENT);
            log.setSentTime(LocalDateTime.now());

        } catch (Exception e) {
            log.setStatus(EmailStatus.FAILED);
            emailLogRepository.save(log);
            throw new EmailFailedException("Failed to send welcome email");
        }

        emailLogRepository.save(log);
    }


    // Method to send password reset email
    public void sendPasswordResetEmail(String email, String resetToken){

        // Construct the reset link with the token
        String resetLink = "http://localhost:6061/reset-password?token=" + resetToken;

        // Load the reset password template and replace placeholders
        String html = templateService.loadResetPasswordTemplate(resetLink);

        // Create a new email log entry for pending status
        EmailLog log = new EmailLog();
        log.setEmail(email);
        log.setMessageType(MessageType.PASSWORD_RESET);
        log.setStatus(EmailStatus.PENDING);

        // Attempt to send the email
        try {
            senderService.sendHtmlEmail(
                    email,
                    "Reset Your Password",
                    html
            );

            // If the email is sent successfully, update the log status to SENT
            log.setStatus(EmailStatus.SENT);
            log.setSentTime(LocalDateTime.now());

        } catch (Exception e) {
            // If an error occurs, update the log status to FAILED and throw a custom exception
            log.setStatus(EmailStatus.FAILED);
            emailLogRepository.save(log);

            // Throw a custom exception to indicate failure
            throw new EmailFailedException("Failed to send password reset email");
        }

        emailLogRepository.save(log);

    }
     /* =========================
         SMS NOTIFICATIONS
         ========================= */
    // method to send phone messages
    public void sendAppointmentSms(
            String phone,
            String name,
            String date,
            String time
    ) {
        log.info("Sending appointment SMS to {}", phone);
        String sms = smsTemplateService.appointmentReminder(name, date, time);

        smsSenderService.sendSms(phone, sms);
    }

    // Todo_reminder SMS
    public void sendTodoReminderSms(TodoReminderRequestDto dto) {

        if (dto.getPhone() == null || dto.getPhone().isBlank()) {
            log.warn(
                    "Skipping TODO reminder SMS. Missing phone number for user={}, task={}",
                    dto.getUserName(),
                    dto.getTaskTitle()
            );

            SmsLog log = new SmsLog();
            log.setPhone(null);
            log.setMessageType(MessageType.TODO_REMINDER);
            log.setStatus(SmsStatus.FAILED);
            log.setSentTime(LocalDateTime.now());

            smsLogRepository.save(log);
            return;
        }

        SmsLog log = new SmsLog();
        log.setPhone(dto.getPhone());
        log.setMessageType(MessageType.TODO_REMINDER);
        log.setStatus(SmsStatus.PENDING);

        try {
            String sms = smsTemplateService.todoReminder(
                    dto.getUserName(),
                    dto.getTaskTitle(),
                    dto.getDueDate(),
                    dto.getPriority()
            );

            smsSenderService.sendSms(dto.getPhone(), sms);

            log.setStatus(SmsStatus.SENT);
            log.setSentTime(LocalDateTime.now());

        } catch (Exception e) {
            log.setStatus(SmsStatus.FAILED);
        }

        smsLogRepository.save(log);
    }


    // Todo_overdue_SMS
    public void sendTodoOverdueSms(TodoOverdueRequestDto dto) {

        // =====================
        // GUARD: phone is required
        // =====================
        if (dto.getPhone() == null || dto.getPhone().isBlank()) {

            log.warn(
                    "Skipping TODO overdue SMS. Missing phone number for user={}, task={}",
                    dto.getUserName(),
                    dto.getTaskTitle()
            );

            SmsLog logEntry = new SmsLog();
            logEntry.setPhone(null);
            logEntry.setMessageType(MessageType.TODO_OVERDUE);
            logEntry.setStatus(SmsStatus.FAILED);
            logEntry.setSentTime(LocalDateTime.now());

            smsLogRepository.save(logEntry);
            return;
        }

        log.info("Sending TODO overdue SMS to {}", dto.getPhone());

        SmsLog logEntry = new SmsLog();
        logEntry.setPhone(dto.getPhone());
        logEntry.setMessageType(MessageType.TODO_OVERDUE);
        logEntry.setStatus(SmsStatus.PENDING);
        smsLogRepository.save(logEntry);

        try {
            String sms = smsTemplateService.todoOverdue(
                    dto.getUserName(),
                    dto.getTaskTitle(),
                    dto.getDueDate()
            );

            smsSenderService.sendSms(dto.getPhone(), sms);

            logEntry.setStatus(SmsStatus.SENT);
            logEntry.setSentTime(LocalDateTime.now());

        } catch (Exception e) {
            log.error(
                    "Failed to send TODO overdue SMS to {}: {}",
                    dto.getPhone(),
                    e.getMessage()
            );
            logEntry.setStatus(SmsStatus.FAILED);
        }

        smsLogRepository.save(logEntry);
    }





//    // Method to send verification email
//    public void sendVerificationEmail(String email, String code) {
//
//        // Build HTML content from template
//        String html = templateService.loadVerificationTemplate(code);
//
//        // Create a new email log entry for default status
//        EmailLog emailLoglog  = new EmailLog();
//        emailLoglog.setEmail(email);
//        emailLoglog.setMessageType(MessageType.VERIFICATION);
//        emailLoglog.setStatus(EmailStatus.PENDING);
//
//        try {
//            // Attempt to send the email
//            senderService.sendHtmlEmail(
//                    email,
//                    "Verify Your Medicare Health Account",
//                    html
//            );
//
//            // If the email is sent successfully, update the log status to SENT
//            emailLoglog.setStatus(EmailStatus.SENT);
//
//            emailLoglog.setSentTime(LocalDateTime.now());
//
//        } catch (Exception e) {
//            // If an error occurs, update the log status to FAILED and throw a custom exception
//            emailLoglog.setStatus(EmailStatus.FAILED);
//            emailLogRepository.save(emailLoglog);
//
//            // Throw a custom exception to indicate failure
//            throw new EmailFailedException("Failed to send verification email: " + e.getMessage());
//        }
//
//        // Save the email log (whether sent or failed)
//        emailLogRepository.save(emailLoglog);
//    }


}
