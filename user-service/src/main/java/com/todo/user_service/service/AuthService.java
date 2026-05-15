package com.todo.user_service.service;

import com.todo.user_service.clients.NotificationServiceClient;
import com.todo.user_service.dto.*;
import com.todo.user_service.entity.User;
import com.todo.user_service.entity.UserStatus;
import com.todo.user_service.exception.*;
import com.todo.user_service.mapper.UserMapper;
import com.todo.user_service.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final UserMapper userMapper;
    private final AuthenticationManager authenticationManager;
    private final NotificationServiceClient notificationService;

    // ── Register ─────────────────────────────────────────────────────────────
    @Transactional
    public RegisterResponseDto registerUser(RegisterRequestDto request) {

        if (userRepository.existsByUsername(request.getUsername())) {
            throw new DuplicateResourceException("User '" + request.getUsername() + "' already exists");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Email '" + request.getEmail() + "' already exists");
        }

        User user = userMapper.toRegisterUserRequest(request);
        user.setStatus(UserStatus.INACTIVE);
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setProfileComplete(false);

        // Save first to get the ID — but transaction will rollback if email fails
        User savedUser = userRepository.save(user);
        log.info("User saved with ID: {}", savedUser.getId());

        // Send verification email — if this throws, @Transactional rolls back the save
        try {
            String verificationCode = notificationService.sendVerificationEmail(
                    new VerificationEmailRequestDto(savedUser.getEmail())
            );
            savedUser.setVerificationCode(verificationCode);
            savedUser.setVerificationCodeExpiresAt(LocalDateTime.now().plusMinutes(15));
            userRepository.save(savedUser);
            log.info("Verification email sent to: {}", savedUser.getEmail());
        } catch (Exception e) {
            log.error("Failed to send verification email to {}: {}", savedUser.getEmail(), e.getMessage());
            // Throwing here causes @Transactional to rollback — user will NOT be saved
            throw new EmailSendFailedException("Failed to send email verification: " + e.getMessage());
        }

        return userMapper.mapToRegisterUserResponse(savedUser, true);
    }

//    /**
//     *
//     * @param request
//     * @return
//     */
//    @Transactional
//    public RegisterResponseDto registerUser(RegisterRequestDto request) {
//
//        if (userRepository.existsByUsername(request.getUsername())) {
//            throw new DuplicateResourceException("User '" + request.getUsername() + "' already exists");
//        }
//        if (userRepository.existsByEmail(request.getEmail())) {
//            throw new DuplicateResourceException("Email '" + request.getEmail() + "' already exists");
//        }
//
//        User user = userMapper.toRegisterUserRequest(request);
//        user.setStatus(UserStatus.INACTIVE);
//        user.setPassword(passwordEncoder.encode(user.getPassword()));
//        // profileComplete starts false — user hasn't set fullName yet
//        user.setProfileComplete(false);
//
//        User savedUser = userRepository.save(user);
//        log.info("User saved with ID: {}", savedUser.getId());
//
//        VerificationEmailRequestDto verificationRequest =
//                new VerificationEmailRequestDto(savedUser.getEmail());
//
//        try {
//            String verificationCode = notificationService.sendVerificationEmail(verificationRequest);
//            savedUser.setVerificationCode(verificationCode);
//            savedUser.setVerificationCodeExpiresAt(LocalDateTime.now().plusMinutes(15));
//            userRepository.save(savedUser);
//            log.info("Verification email sent to: {}", savedUser.getEmail());
//        } catch (Exception e) {
//            throw new EmailSendFailedException("Failed to send email verification: " + e.getMessage());
//        }
//
//        return userMapper.mapToRegisterUserResponse(savedUser, true);
//    }

    // ── Login ────────────────────────────────────────────────────────────────

    public LoginResponseDto loginUser(LoginRequestDto loginRequestDto) {

        User user = userRepository.findByUsername(loginRequestDto.getUsername())
                .orElseThrow(() -> new NotFoundException(
                        "User with name " + loginRequestDto.getUsername() + " not found"));

        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new NotFoundException("Account not verified or inactive. Please verify your account.");
        }

        if (!passwordEncoder.matches(loginRequestDto.getPassword(), user.getPassword())) {
            throw new InvalidCredentialsException("Incorrect password. Please try again.");
        }

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequestDto.getUsername(),
                        loginRequestDto.getPassword()
                )
        );

        log.info("User '{}' authenticated. profileComplete={}",
                loginRequestDto.getUsername(), user.isProfileComplete());

        String token = jwtService.generateToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);

        // profileComplete is included in the response — frontend routes based on this
        return userMapper.toLoginUserResponse(user, token, refreshToken);
    }

    // ── Verify email ─────────────────────────────────────────────────────────

    @Transactional
    public VerifyEmailResponseDto verifyUser(VerifyEmailRequestDto verifyEmailRequestDto) {

        User user = userRepository.findById(verifyEmailRequestDto.getUserId())
                .orElseThrow(() -> new NotFoundException("User not found"));

        if (user.getStatus() == UserStatus.ACTIVE) {
            throw new AccountAlreadyVerifiedException("Account already verified");
        }

        if (user.getVerificationCode() == null ||
                !user.getVerificationCode().equals(verifyEmailRequestDto.getVerificationCode())) {
            throw new NotFoundException("Invalid verification code");
        }

        if (user.getVerificationCodeExpiresAt() == null ||
                user.getVerificationCodeExpiresAt().isBefore(LocalDateTime.now())) {
            throw new VerificationCodeExpiredException("Verification code has expired");
        }

        user.setStatus(UserStatus.ACTIVE);
        user.setVerificationCode(null);
        user.setVerificationCodeExpiresAt(null);
        userRepository.save(user);

        log.info("User '{}' verified successfully", user.getUsername());

        try {
            notificationService.sendWelcomeEmail(new WelcomeEmailRequestDto(user.getEmail()));
            log.info("Welcome email sent to {}", user.getEmail());
        } catch (Exception e) {
            throw new EmailSendFailedException(
                    "Failed to send welcome email to: " + user.getEmail() + " — " + e.getMessage());
        }

        return userMapper.verifyEmailResponse(true, "Email verified successfully");
    }

    // ── Resend verification ───────────────────────────────────────────────────

    @Transactional
    public ResendVerificationResponseDto resendVerificationCode(ResendVerificationRequestDto requestDto) {

        User user = userRepository.findByEmail(requestDto.getEmail())
                .orElseThrow(() -> new NotFoundException("Email not found"));

        if (user.getStatus() == UserStatus.ACTIVE) {
            throw new AccountAlreadyVerifiedException("User already verified");
        }

        try {
            String newCode = notificationService.sendVerificationEmail(
                    new VerificationEmailRequestDto(user.getEmail()));
            user.setVerificationCode(newCode);
            user.setVerificationCodeExpiresAt(LocalDateTime.now().plusMinutes(15));
            userRepository.save(user);
            log.info("New verification code resent to '{}'", user.getEmail());
            return userMapper.resendVerificationResponse(true, "Verification code resent successfully", user.getId());
        } catch (Exception e) {
            throw new EmailSendFailedException("Failed to resend verification email: " + e.getMessage());
        }
    }

    // ── Password reset request ────────────────────────────────────────────────

    @Transactional
    public PasswordResetResponseDto requestPasswordReset(PasswordResetRequestDto request) {

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new NotFoundException("User with this email not found"));

        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new AccountNotVerifiedException(
                    "Your account has not been verified yet. " +
                            "Please check your email for the verification code or request a new one.");
        }

        String resetToken = UUID.randomUUID().toString();
        user.setPasswordResetToken(resetToken);
        user.setPasswordResetTokenExpiresAt(LocalDateTime.now().plusMinutes(15));
        userRepository.save(user);

        try {
            notificationService.sendPasswordResetEmail(
                    new PasswordResetEmailRequestDto(user.getEmail(), resetToken));
            log.info("Password reset link sent to {}", user.getEmail());
            return new PasswordResetResponseDto(true, "Password reset link sent to your email.");
        } catch (Exception e) {
            throw new EmailSendFailedException("Failed to send password reset email: " + e.getMessage());
        }
    }

    // ── Reset password ────────────────────────────────────────────────────────

    @Transactional
    public PasswordResetResponseDto resetPassword(ResetPasswordDto request) {

        User user = userRepository.findByPasswordResetToken(request.getToken())
                .orElseThrow(() -> new NotFoundException("Invalid reset token"));

        if (user.getPasswordResetTokenExpiresAt().isBefore(LocalDateTime.now())) {
            throw new VerificationCodeExpiredException("Reset token expired");
        }

        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new InvalidCredentialsException("Passwords do not match");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        user.setPasswordResetToken(null);
        user.setPasswordResetTokenExpiresAt(null);
        userRepository.save(user);

        return new PasswordResetResponseDto(true, "Password reset successful");
    }

    // ── Update profile ────────────────────────────────────────────────────────

    /**
     * User-facing profile update. Accepts fullName and phoneNumber only.
     * Marks profileComplete = true once fullName is provided.
     * This is the endpoint the frontend calls after registration or OAuth sign-in.
     */
    @Transactional
    public UserResponseDto updateUserProfile(CompleteUserRequestDto request, UUID id) {

        User user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User not found"));

        // mapToCompleteUserRequest handles null-safety and sets profileComplete = true
        userMapper.mapToCompleteUserRequest(request, user);

        User updatedUser = userRepository.save(user);
        log.info("Profile updated for user {} — profileComplete={}",
                updatedUser.getUsername(), updatedUser.isProfileComplete());

        return userMapper.mapToUserResponse(updatedUser);
    }

}