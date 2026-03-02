package com.todo.notification_service.service;

import com.todo.notification_service.exception.TemplateLoadException;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

@Service
@AllArgsConstructor
public class EmailTemplateService {

    // load template from a given file path
    private String loadTemplate(String path) {
        try {
            ClassPathResource resource = new ClassPathResource(path);
            return new String(
                    resource.getInputStream().readAllBytes(),
                    StandardCharsets.UTF_8
            );
        } catch (IOException e) {
            throw new TemplateLoadException("Failed to load email template");
        }
    }

//  Method to load the verification email template and replace placeholders
    public String loadVerificationTemplate(String code) {
        return loadTemplate("templates/email/verification-email.html")
                .replace("{{CODE}}", code)
                .replace("{{YEAR}}", String.valueOf(LocalDateTime.now().getYear()));
    }

// Method to load the welcome email template and replace the year placeholder
    public String loadWelcomeTemplate() {
        return loadTemplate("templates/email/welcome-email.html")
                .replace("{{YEAR}}", String.valueOf(LocalDateTime.now().getYear()));
    }

    // Method to load the password reset email template and replace the reset link and year
    public String loadResetPasswordTemplate(String resetLink) {
        return loadTemplate("templates/email/reset-password.html")
                .replace("{{RESET_LINK}}", resetLink)
                .replace("{{YEAR}}", String.valueOf(LocalDateTime.now().getYear()));
    }

}
