package com.todo_service.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.UUID;

public final class JwtUtils {

    private JwtUtils() {}

    private static JwtUserPrincipal principal() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalStateException("User not authenticated");
        }

        return (JwtUserPrincipal) authentication.getPrincipal();
    }

    public static UUID getCurrentUserId() {
        return principal().userId();
    }

    public static String getCurrentUserName() {
        return principal().username();
    }
}
