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
        description = "Email and SMS notification operations"
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
                ApiResponse.buildResponse(
                        null,
                        HttpStatus.OK.value(),
                        "Welcome email sent successfully"
                )
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
        notificationService.sendPasswordResetEmail(
                request.getEmail(),
                request.getResetToken()
        );

        return ResponseEntity.ok(
                ApiResponse.buildResponse(
                        null,
                        HttpStatus.OK.value(),
                        "Password reset email sent successfully"
                )
        );
    }

    /* =========================
        SMS ENDPOINTS
       ========================= */

    @Operation(
            summary = "Send appointment SMS",
            description = "Send an SMS notification for an appointment reminder"
    )
    @PostMapping("/appointment-sms")
    public ResponseEntity<ResponseDto> sendAppointmentSms(
            @RequestBody AppointmentSmsRequestDto request
    ) {
        notificationService.sendAppointmentSms(
                request.getPhone(),
                request.getName(),
                request.getDate(),
                request.getTime()
        );

        return ResponseEntity.ok(
                ApiResponse.buildResponse(
                        null,
                        HttpStatus.OK.value(),
                        "Appointment SMS sent successfully"
                )
        );
    }

    @Operation(
            summary = "Send todo reminder SMS",
            description = "Send an SMS reminder for a scheduled todo task"
    )
    @PostMapping("/todo-reminder-sms")
    public ResponseEntity<ResponseDto> sendTodoReminderSms(
            @RequestBody TodoReminderRequestDto request
    ) {
        notificationService.sendTodoReminderSms(request);

        return ResponseEntity.ok(
                ApiResponse.buildResponse(
                        null,
                        HttpStatus.OK.value(),
                        "Todo reminder SMS sent successfully"
                )
        );
    }

    @Operation(
            summary = "Send overdue todo SMS",
            description = "Send an SMS notification for overdue todo tasks"
    )
    @PostMapping("/todo-overdue-sms")
    public ResponseEntity<ResponseDto> sendTodoOverdueSms(
            @RequestBody TodoOverdueRequestDto request
    ) {
        notificationService.sendTodoOverdueSms(request);

        return ResponseEntity.ok(
                ApiResponse.buildResponse(
                        null,
                        HttpStatus.OK.value(),
                        "Todo overdue SMS sent successfully"
                )
        );
    }
}
