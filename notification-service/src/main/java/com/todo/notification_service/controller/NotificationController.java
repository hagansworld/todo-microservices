package com.todo.notification_service.controller;

import com.todo.notification_service.dto.*;
import com.todo.notification_service.response.ApiResponse;
import com.todo.notification_service.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(
        name = "Notification Service",
        description = "Email notification operations"
)
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/notifications")
@SecurityRequirement(name = "bearerAuth")
public class NotificationController {

    private final NotificationService notificationService;

    /* =========================
       EMAIL ENDPOINTS
       ========================= */

    @Operation(
            summary = "Send verification email",
            description = "Send an email containing a verification code to a user"
    )
    @PostMapping("/send-verification")
    public ResponseEntity<String> sendVerificationEmail(
            @RequestBody VerificationEmailRequestDto request
    ) {
        String code = notificationService.sendVerificationEmail(request.getEmail());
        return ResponseEntity.ok(code);
    }

    @Operation(
            summary = "Send welcome email",
            description = "Send a welcome email to a newly registered user"
    )
    @PostMapping("/send-welcome")
    public ResponseEntity<ResponseDto> sendWelcome(
            @RequestBody WelcomeEmailRequestDto request
    ) {
        notificationService.sendWelcomeEmail(request.getEmail());
        return ResponseEntity.ok(
                ApiResponse.buildResponse(null, HttpStatus.OK.value(), "Welcome email sent successfully")
        );
    }

    @Operation(
            summary = "Send password reset email",
            description = "Send a password reset email with a reset token"
    )
    @PostMapping("/send-password-reset")
    public ResponseEntity<ResponseDto> sendPasswordResetEmail(
            @RequestBody PasswordResetEmailRequestDto request
    ) {
        notificationService.sendPasswordResetEmail(request.getEmail(), request.getResetToken());
        return ResponseEntity.ok(
                ApiResponse.buildResponse(null, HttpStatus.OK.value(), "Password reset email sent successfully")
        );
    }

    @Operation(
            summary = "Send todo reminder email",
            description = "Send an email reminder for a scheduled todo task"
    )
    @PostMapping("/todo-reminder-email")
    public ResponseEntity<ResponseDto> sendTodoReminderEmail(
            @RequestBody TodoReminderRequestDto request
    ) {
        notificationService.sendTodoReminderEmail(request);
        return ResponseEntity.ok(
                ApiResponse.buildResponse(null, HttpStatus.OK.value(), "Todo reminder email sent successfully")
        );
    }

    @Operation(
            summary = "Send overdue todo email",
            description = "Send an email notification for overdue todo tasks"
    )
    @PostMapping("/todo-overdue-email")
    public ResponseEntity<ResponseDto> sendTodoOverdueEmail(
            @RequestBody TodoOverdueRequestDto request
    ) {
        notificationService.sendTodoOverdueEmail(request);
        return ResponseEntity.ok(
                ApiResponse.buildResponse(null, HttpStatus.OK.value(), "Todo overdue email sent successfully")
        );
    }
}