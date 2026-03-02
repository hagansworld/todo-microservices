package com.todo.notification_service.service;

import org.springframework.stereotype.Service;

import java.security.SecureRandom;

@Service
public class VerificationCodeService {
    private final SecureRandom secureRandom = new SecureRandom();

    // generate random code
        public String generateCode() {
            int code = secureRandom.nextInt(900000) + 100000;
            return String.valueOf(code);
        }

}
