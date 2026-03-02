package com.todo_service.config;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.Date;
import java.util.List;

@Component
@RefreshScope
public class ServiceJwtProvider {

    private final SecretKey secretKey;

    public ServiceJwtProvider(
            @Value("${spring.security.jwt.secretKey}") String secretKey
    ) {
        this.secretKey = Keys.hmacShaKeyFor(secretKey.getBytes());
    }
    public String generateToken() {

        Instant now = Instant.now();

        return Jwts.builder()
                .subject("todo-service") // SERVICE identity
                .claim("username", "todo-service")
                .claim("roles", List.of("ROLE_SERVICE"))
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusSeconds(60 * 60))) // 1 hour
                .signWith(secretKey) // algorithm inferred (HS256)
                .compact();
    }
}
