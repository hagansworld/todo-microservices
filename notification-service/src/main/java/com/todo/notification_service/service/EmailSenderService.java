package com.todo.notification_service.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

/**
 * Sends HTML emails via the Resend HTTP API.
 * Drop-in replacement for the JavaMailSender-based implementation.
 * No SMTP ports needed — works on Render free tier.
 *
 * Resend API docs: https://resend.com/docs/api-reference/emails/send-email
 */
@Service
@Slf4j
public class EmailSenderService {

    private static final String RESEND_API_URL = "https://api.resend.com/emails";

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${resend.api-key}")
    private String apiKey;

    @Value("${resend.from-address}")
    private String fromAddress;

    /**
     * Sends an HTML email via Resend.
     * Signature matches the old JavaMailSender version — callers need no changes.
     *
     * @param to      recipient email address
     * @param subject email subject line
     * @param html    full HTML body (your existing templates)
     */
    public void sendHtmlEmail(String to, String subject, String html) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);

        Map<String, Object> body = Map.of(
                "from",    fromAddress,
                "to",      new String[]{ to },
                "subject", subject,
                "html",    html
        );

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<Map> response =
                    restTemplate.postForEntity(RESEND_API_URL, request, Map.class);

            log.info("Email sent to {} via Resend — status={}, id={}",
                    to, response.getStatusCode(),
                    response.getBody() != null ? response.getBody().get("id") : "n/a");

        } catch (Exception e) {
            log.error("Resend API call failed for {}: {}", to, e.getMessage());
            // Preserve the checked-exception contract the callers expect
            throw new RuntimeException("Failed to send email via Resend: " + e.getMessage(), e);
        }
    }
}