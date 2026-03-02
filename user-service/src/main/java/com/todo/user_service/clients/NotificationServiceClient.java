package com.todo.user_service.clients;

import com.todo.user_service.dto.PasswordResetEmailRequestDto;
import com.todo.user_service.dto.VerificationEmailRequestDto;
import com.todo.user_service.dto.WelcomeEmailRequestDto;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;

@HttpExchange("/api/notifications")
public interface NotificationServiceClient {
    @PostExchange("/send-verification")
    String sendVerificationEmail(@RequestBody VerificationEmailRequestDto request);

    @PostExchange("/send-welcome")
    void sendWelcomeEmail(@RequestBody WelcomeEmailRequestDto request);

    @PostExchange("/send-password-reset")
    void sendPasswordResetEmail(@RequestBody PasswordResetEmailRequestDto request);
}
