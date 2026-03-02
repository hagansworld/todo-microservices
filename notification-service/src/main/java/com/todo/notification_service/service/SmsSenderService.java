package com.todo.notification_service.service;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Service;

@Service
@RefreshScope
public class SmsSenderService {
    @Value("${spring.twilio.account-sid}")
    private String accountSid;

    @Value("${spring.twilio.auth-token}")
    private String authToken;

    @Value("${spring.twilio.phone-number}")
    private String twilioPhoneNumber;

    // Initialize Twilio SDK
    @PostConstruct
    public void init() {
        Twilio.init(accountSid, authToken);
    }

    public void sendSms(String phone, String message) {
        Message.creator(
                new PhoneNumber(phone),// Recipient's phone number
                new PhoneNumber(twilioPhoneNumber), // Twilio phone number
                message // content message
        ).create();
    }
}
