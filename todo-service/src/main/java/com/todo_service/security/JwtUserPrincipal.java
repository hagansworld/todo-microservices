package com.todo_service.security;

import java.io.Serializable;
import java.util.UUID;

public record JwtUserPrincipal(
        UUID userId,
        String username
) implements Serializable {}
