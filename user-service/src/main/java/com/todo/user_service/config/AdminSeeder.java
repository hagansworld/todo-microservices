package com.todo.user_service.config;

import com.todo.user_service.entity.User;
import com.todo.user_service.entity.UserRole;
import com.todo.user_service.entity.UserStatus;
import com.todo.user_service.repository.UserRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class AdminSeeder {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${admin.seed.email}")
    private String adminEmail;

    @Value("${admin.seed.password}")
    private String adminPassword;

    @PostConstruct
    public void seedAdmin() {

        if (userRepository.existsByEmail(adminEmail)) {
            log.info("Admin user already exists, skipping seeding");
            return;
        }

        User user = new User();
        user.setUsername("admin");
        user.setFullName("System Admin");
        user.setEmail(adminEmail);
        user.setPassword(passwordEncoder.encode(adminPassword));
        user.setRole(UserRole.ADMIN);
        user.setStatus(UserStatus.ACTIVE);

        userRepository.save(user);

        log.info("Admin user created successfully: {}", adminEmail);
    }
}