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

    /**
     * Register User
     * @param request - request Object to register Users
     * @return RegisterResponseDto
     * @author - Isaac Hagan
     */
    @Transactional
    public RegisterResponseDto registerUser(RegisterRequestDto request) {

        // check if username exists
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new DuplicateResourceException("User '" + request.getUsername() + "' already exists");
        }

        // check if email exists
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Email '" + request.getEmail() + "' already exists");
        }

        // convert registration Dto to RegisterUserRequest
        User user = userMapper.toRegisterUserRequest(request);
        user.setStatus(UserStatus.INACTIVE);
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        // Save user first
        User savedUser = userRepository.save(user);
        log.info("User saved with ID: {}", savedUser.getId());

        // Prepare the request Dto to send to notification service
        VerificationEmailRequestDto verificationRequest = new VerificationEmailRequestDto(savedUser.getEmail());

        try {
            // Call the Notification Service to send the verification email
            String verificationCode = notificationService.sendVerificationEmail(verificationRequest);
            log.info("Verification email sent successfully to: {}", savedUser.getEmail());
            // Save ONLY domain data
            savedUser.setVerificationCode(verificationCode);

            // set expiration code
            savedUser.setVerificationCodeExpiresAt(
                    LocalDateTime.now().plusMinutes(15)
            );


            userRepository.save(savedUser);

        } catch (Exception e) {
            throw new EmailSendFailedException("Failed to send email verification: " + e.getMessage());
        }

        return userMapper.mapToRegisterUserResponse(savedUser, true);
    }

    /**
     * Login User
     * @param loginRequestDto - loginRequestDto Object for loginUser
     * @return LoginResponseDto
     * @author - Isaac Hagan
     */

    public LoginResponseDto loginUser(LoginRequestDto loginRequestDto) {
        // Find user by username
        User user = userRepository.findByUsername(loginRequestDto.getUsername())
                .orElseThrow(() -> new NotFoundException("User with name " + loginRequestDto.getUsername() + " not found"));

        // Check if the user's account status is ACTIVE (assuming "ACTIVE" is the value for active users)
        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new NotFoundException("Account not verified or inactive. Please verify your account.");
        }

        // Validate the password
        if (!passwordEncoder.matches(loginRequestDto.getPassword(), user.getPassword())) {
            throw new InvalidCredentialsException("Incorrect password. Please try again.");
        }

        // Authenticate the user credentials using Spring Security
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequestDto.getUsername(),
                        loginRequestDto.getPassword()
                )
        );
        log.info("User '{}' authenticated successfully", loginRequestDto.getUsername());

        // Generate JWT token
        String token = jwtService.generateToken(user);

        // Generate refresh token
        String refreshToken = jwtService.generateRefreshToken(user);

        // Map the response to LoginResponseDto
        return userMapper.toLoginUserResponse(user, token, refreshToken);
    }


    /**
     * VerifyUser
     * @param verifyEmailRequestDto - verifyEmailRequest Object for User
     * @return verifyEmailResponse
     * @author - Isaac Hagan
     */
    @Transactional
    public VerifyEmailResponseDto verifyUser(VerifyEmailRequestDto verifyEmailRequestDto) {
        // fetch User By id
        User user = userRepository.findById(verifyEmailRequestDto.getUserId())
                .orElseThrow(()-> new NotFoundException("User not found"));

        // check if  user already verified
        if (user.getStatus() == UserStatus.ACTIVE){
            throw new AccountAlreadyVerifiedException("Account already Verified");
        }

        // validate the code(only user with code can verify, other users with same code cant verify)
        if (user.getVerificationCode() == null ||
                !user.getVerificationCode().equals(verifyEmailRequestDto.getVerificationCode())) {
            throw new NotFoundException("Invalid verification code");
        }

        // Check if the verification code has expired
        if (user.getVerificationCodeExpiresAt() == null || user.getVerificationCodeExpiresAt().isBefore(LocalDateTime.now())) {
            throw new VerificationCodeExpiredException("Verification code has expired");
        }

        // Update the user's status to ACTIVE after successful verification
        user.setStatus(UserStatus.ACTIVE);

        // Clear the verification code and expiration
        user.setVerificationCode(null);
        user.setVerificationCodeExpiresAt(null);

        // Save the updated user info
        userRepository.save(user);
        log.info("User '{}' verification completed and updated in the database", user.getUsername());
        try{
            // prepare welcome email request to be sent to notification service
            WelcomeEmailRequestDto request = new WelcomeEmailRequestDto(user.getEmail());
            // Send welcome email (NON-BLOCKING)
            notificationService.sendWelcomeEmail(request);
            log.info("Welcome email sent to {} ", user.getEmail());

        } catch (Exception e) {
            throw new EmailSendFailedException("Failed to send welcome email to : {} " + user.getEmail() + e.getMessage());
        }

        // Return a success response
        return userMapper.verifyEmailResponse(true,"Email verified successfully");
    }
    /**
     * Resend Verification Code
     * @param requestDto - requestDto Object of user
     * @return ResendVerificationResponseDto
     * @author - Isaac Hagan
     */
    @Transactional
    public ResendVerificationResponseDto resendVerificationCode(ResendVerificationRequestDto requestDto) {

        // check if email exist
        User user = userRepository.findByEmail(requestDto.getEmail())
                .orElseThrow(() -> new NotFoundException("Email not found"));

        // check if status of the user is Active
        if (user.getStatus() == UserStatus.ACTIVE) {
            throw new AccountAlreadyVerifiedException("User already verified");
        }

        // prepare email  for notification service
        VerificationEmailRequestDto verificationRequest = new VerificationEmailRequestDto(user.getEmail());

        try {
            //  notification generate a NEW code to be sent to the email in the request
            String newCode = notificationService.sendVerificationEmail(verificationRequest);

            // set the new verification code
            user.setVerificationCode(newCode);

            // new verification to expire after 15 minutes
            user.setVerificationCodeExpiresAt(LocalDateTime.now().plusMinutes(15));

            // save the new code with user
            userRepository.save(user);
            log.info("New verification code resent to '{}'", user.getEmail());

            return userMapper.resendVerificationResponse(true, "Verification code resent successfully");

        } catch (Exception e) {
            throw new EmailSendFailedException(
                    "Failed to resend verification email: " + e.getMessage());
        }
    }

    /**
     *  starts the process to reset a user's password by sending email to users
     * @param request -  email of the user
     * @return PasswordResetRepose
     *  @author - Isaac Hagan
     */
    @Transactional
    public PasswordResetResponseDto requestPasswordReset(PasswordResetRequestDto request) {
        // Fetch the user by email
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new NotFoundException("User with this email not found"));

        // Generate a unique reset token
        String resetToken = UUID.randomUUID().toString();

        // Save the token in the user's record with an expiration time
        user.setPasswordResetToken(resetToken);
        user.setPasswordResetTokenExpiresAt(LocalDateTime.now().plusMinutes(15));
        userRepository.save(user);

        // prepare to send the token via email (with a reset token to be sent to notification service)
        PasswordResetEmailRequestDto emailRequest = new PasswordResetEmailRequestDto(user.getEmail(), resetToken);

        try {
            // This should send the email with the reset token
            notificationService.sendPasswordResetEmail(emailRequest);
            log.info("Password reset link sent to {}", user.getEmail());

            return new PasswordResetResponseDto(true, "Password reset link sent to your email.");
        } catch (Exception e) {
            throw new EmailSendFailedException("Failed to send password reset email: " + e.getMessage());
        }
    }

    /**
     * Resets the user's password using a provided reset token and new password details.
     * @param request - Request containing the reset token, new password, and confirmation password.
     * @return PasswordResetResponseDto
     * @author Isaac Hagan
     */
    @Transactional
    public PasswordResetResponseDto resetPassword(ResetPasswordDto request) {
        // checks if user with token exist
        User user = userRepository.findByPasswordResetToken(request.getToken())
                .orElseThrow(() -> new NotFoundException("Invalid reset token"));

        // checks if reset token has expired
        if (user.getPasswordResetTokenExpiresAt().isBefore(LocalDateTime.now())) {
            throw new VerificationCodeExpiredException("Reset token expired");
        }

        // checks if the second password matches with the first password
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new InvalidCredentialsException("Passwords do not match");
        }

        // set our new password and set the second resetToken and expiration date to null
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        user.setPasswordResetToken(null);
        user.setPasswordResetTokenExpiresAt(null);

        // save the new updated password
        userRepository.save(user);

        return new PasswordResetResponseDto(true, "Password reset successful");
    }

    /**
     * Update User Profile after successful login.
     * @param completeUserRequestDto Contains the new profile data (full name, phone number, address)
     * @param id The ID of the logged-in user (extracted from JWT or session)
     * @return Updated UserResponseDto containing the updated profile data.
     *  @author - Isaac Hagan
     */
    public UserResponseDto updateUserProfile(CompleteUserRequestDto completeUserRequestDto, UUID id) {
        // check if user exist
        User user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User not found"));

        // check if there is no user
        if (user.getId() == null){
            throw new NotFoundException("User must not be null");
        }

        // Update user profile with the new information
        if (completeUserRequestDto.getFullName() != null) {
            user.setFullName(completeUserRequestDto.getFullName());
        }
        if (completeUserRequestDto.getPhoneNumber() != null) {
            user.setPhoneNumber(completeUserRequestDto.getPhoneNumber());
        }

        // update the user
        userMapper.mapToCompleteUserRequest(completeUserRequestDto, user);

        // Save the updated user profile to the database
        User updatedUser = userRepository.save(user);
        log.info("User updated successfully {} ", updatedUser);

        // Map the updated user to a UserResponseDto
        return userMapper.mapToUserResponse(updatedUser);
    }


}
