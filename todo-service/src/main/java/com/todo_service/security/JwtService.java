package com.todo_service.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.function.Function;

@Service
public class JwtService {

    @Value("${spring.security.jwt.secretKey}")
    private String secretKey;

    /* ================== TOKEN PARSING ================== */

    public String extractUserId(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public String extractUsername(String token) {
        return extractAllClaims(token).get("username", String.class);
    }

    public Collection<SimpleGrantedAuthority> extractRoles(String token) {
        List<?> roles = extractAllClaims(token).get("roles", List.class);
        return roles.stream()
                .map(Object::toString)
                .map(SimpleGrantedAuthority::new)
                .toList();
    }

    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public <T> T extractClaim(String token, Function<Claims, T> resolver) {
        return resolver.apply(extractAllClaims(token));
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSignInKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private SecretKey getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /* ================== NEW: SYSTEM TOKEN GENERATION ================== */

    /**
     * Generates a JWT token for service-to-service calls (e.g., todo-service -> user-service)
     */
    public String generateTokenForService() {
        long now = System.currentTimeMillis();
        long expiry = now + 24 * 60 * 60 * 1000; // 24 hours

        return Jwts.builder()
                .subject("todo-service") // SERVICE identity
                .claim("username", "todo-service")
                .claim("roles", List.of("ROLE_SERVICE"))
                .issuedAt(new Date(now))
                .expiration(new Date(expiry))
                .signWith(getSignInKey())
                .compact();
    }
}


