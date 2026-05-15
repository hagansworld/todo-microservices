package com.todo.user_service.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;
    private final AuthenticationProvider authenticationProvider;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
                // ── CORS is handled entirely by the gateway — disable it here ──
                .cors(AbstractHttpConfigurer::disable)
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth

                        // ── Swagger (public) ──────────────────────────────
                        .requestMatchers(
                                "/swagger-ui/**",
                                "/v3/api-docs",
                                "/v3/api-docs/**",
                                "/api-docs",
                                "/api-docs/**"
                        ).permitAll()

                        // ── Auth (public) ─────────────────────────────────
                        .requestMatchers(
                                "/api/auth/register-user",
                                "/api/auth/login-user",
                                "/api/auth/verify-user",
                                "/api/auth/resend-verification",
                                "/api/auth/request-password-reset",
                                "/api/auth/reset-password",
                                "/api/auth/google/signup",
                                "/api/auth/google/signin"
                        ).permitAll()

                        // ── Auth protected ────────────────────────────────
                        .requestMatchers("/api/auth/update-profile/**").authenticated()

                        // ── Preferences: USER (frontend) or SERVICE (scheduler) ──
                        .requestMatchers("/api/users/*/preferences/**").hasAnyRole("USER", "SERVICE")
                        .requestMatchers("/api/users/preferences/**").hasAnyRole("USER", "SERVICE")

                        // ── Everything else under /api/users/** — ADMIN or SERVICE ──
                        .requestMatchers("/api/users/**").hasAnyRole("ADMIN", "SERVICE")

                        .requestMatchers("/actuator/health", "/actuator/info").permitAll()

                        .anyRequest().authenticated()
                )
                .authenticationProvider(authenticationProvider)
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}