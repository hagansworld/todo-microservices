package com.todo.user_service.service;

import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.todo.user_service.clients.NotificationServiceClient;
import com.todo.user_service.dto.GoogleAuthResponseDto;
import com.todo.user_service.dto.WelcomeEmailRequestDto;
import com.todo.user_service.entity.User;
import com.todo.user_service.entity.UserStatus;
import com.todo.user_service.exception.GoogleAuthException;
import com.todo.user_service.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;

import java.util.Collections;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class GoogleOAuthService {

    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final NotificationServiceClient notificationServiceClient;

    @Value("${google.oauth2.client-id}")
    private String googleClientId;

    /**
     * Handles both Google Sign-Up and Sign-In.
     *
     * Flow:
     * 1. Verify the idToken against Google's public keys
     * 2. Extract profile (email, name, picture) from verified payload
     * 3. Check if user exists by email:
     *    - NOT FOUND → signup: create user, send welcome email
     *    - FOUND     → login: return tokens directly
     * 4. Generate and return JWT tokens either way
     *
     * @param idToken - raw Google ID token from the frontend
     * @return GoogleAuthResponseDto
     * @author Isaac Hagan
     */
    @Transactional
    public GoogleAuthResponseDto handleGoogleAuth(String idToken) {

        // Verify token with Google — throws GoogleAuthException if invalid
        GoogleIdToken.Payload payload = verifyGoogleToken(idToken);

        // Extract profile from verified payload
        String googleSub = payload.getSubject();
        String email     = payload.getEmail();
        String fullName  = (String) payload.get("name");
        String avatarUrl = (String) payload.get("picture");

        log.info("Google OAuth request — email={}", email);

        Optional<User> existingUser = userRepository.findByEmail(email);
        boolean isNewUser = existingUser.isEmpty();
        User user;

        if (isNewUser) {
            // ── SIGNUP PATH ───────────────────────────────────────────────
            user = createGoogleUser(email, fullName, avatarUrl, googleSub);
            log.info("New user registered via Google OAuth — email={}", email);

            // Welcome email is non-blocking — failure won't break signup
            sendWelcomeEmail(email);

        } else {
            // ── LOGIN PATH ────────────────────────────────────────────────
            user = existingUser.get();

            /*
             * Edge case: user originally signed up with email/password,
             * now signing in with Google for the first time.
             * Link the accounts without forcing re-registration.
             */
            if (!user.isOauthUser()) {
                user.setOauthProvider("google");
                user.setOauthProviderId(googleSub);
                user.setOauthUser(true);

                if (avatarUrl != null && user.getAvatarUrl() == null) {
                    user.setAvatarUrl(avatarUrl);
                }
                userRepository.save(user);
                log.info("Google account linked to existing account — email={}", email);
            }

            log.info("Existing user signed in via Google — email={}", email);
        }

        String accessToken  = jwtService.generateToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);

        return GoogleAuthResponseDto.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .userId(user.getId().toString())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .avatarUrl(user.getAvatarUrl())
                .isNewUser(isNewUser)
                .build();
    }

    /* =====================================================================
       PRIVATE HELPERS
    ===================================================================== */

    /**
     * Verifies the raw Google ID token using Google's public keys.
     * Validates the audience (your client ID) to prevent token reuse attacks.
     *
     * Uses NetHttpTransport + GsonFactory — both safe with Spring Boot 4.x
     * and the google-auth-library dependency.
     *
     * @param idToken - raw token string from frontend
     * @return verified GoogleIdToken.Payload
     * @throws GoogleAuthException if token is null, expired, or tampered
     * @author Isaac Hagan
     */
    private GoogleIdToken.Payload verifyGoogleToken(String idToken) {
        try {
            GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier
                    .Builder(new NetHttpTransport(), GsonFactory.getDefaultInstance())
                    .setAudience(Collections.singletonList(googleClientId))
                    .build();

            GoogleIdToken googleIdToken = verifier.verify(idToken);

            if (googleIdToken == null) {
                throw new GoogleAuthException("Invalid or expired Google ID token");
            }

            return googleIdToken.getPayload();

        } catch (GoogleAuthException e) {
            throw e; // already our exception — rethrow as-is
        } catch (Exception e) {
            log.error("Google token verification failed: {}", e.getMessage());
            throw new GoogleAuthException("Google token verification failed: " + e.getMessage());
        }
    }

    /**
     * Builds and persists a new User from Google profile data.
     *
     * Status → ACTIVE immediately.
     * Google already verified the email, so our verification step is skipped.
     * Password → null (OAuth-only account, no password login possible).
     *
     * Username is derived from email prefix + first 5 chars of googleSub
     * to satisfy the DB unique constraint without asking the user to choose.
     * e.g. "isaac@gmail.com" + sub "11482..." → username "isaac_11482"
     *
     * @author Isaac Hagan
     */
    private User createGoogleUser(
            String email,
            String fullName,
            String avatarUrl,
            String googleSub
    ) {
        User user = new User();
        user.setEmail(email);
        user.setFullName(fullName != null ? fullName : email);
        user.setAvatarUrl(avatarUrl);
        user.setOauthProvider("google");
        user.setOauthProviderId(googleSub);
        user.setOauthUser(true);
        user.setStatus(UserStatus.ACTIVE);
        user.setPassword(null);

        // Use the full googleSub — it's Google's own unique permanent ID,
        // guaranteed globally unique. No collision possible.
        // User never sees this username, so readability doesn't matter.
        String uniqueUsername = email.split("@")[0] + "_" + googleSub;
        user.setUsername(uniqueUsername);

        return userRepository.save(user);
    }

    /**
     * Fires the welcome email via notification service.
     * Wrapped in try/catch — notification failure must never block signup.
     *
     * @author Isaac Hagan
     */
    private void sendWelcomeEmail(String email) {
        try {
            WelcomeEmailRequestDto request = new WelcomeEmailRequestDto(email);
            notificationServiceClient.sendWelcomeEmail(request);
            log.info("Welcome email dispatched — email={}", email);
        } catch (Exception e) {
            log.error("Welcome email failed (non-blocking) — email={}, reason={}", email, e.getMessage());
        }
    }
}