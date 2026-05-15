package com.todo.user_service.mapper;

import com.todo.user_service.dto.*;
import com.todo.user_service.entity.Address;
import com.todo.user_service.entity.User;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class UserMapper {

    // ── Registration ─────────────────────────────────────────────────────────

    public RegisterResponseDto mapToRegisterUserResponse(User user, boolean emailSent) {
        RegisterResponseDto response = new RegisterResponseDto();
        response.setId(user.getId());
        response.setUsername(user.getUsername());
        response.setEmail(user.getEmail());
        response.setEmailSent(emailSent);
        response.setMessage(emailSent ? "Verification email sent" : "Failed to send email");
        return response;
    }

    public User toRegisterUserRequest(RegisterRequestDto request) {
        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(request.getPassword());
        return user;
    }

    // ── Login ────────────────────────────────────────────────────────────────

    public LoginResponseDto toLoginUserResponse(User user, String token, String refreshToken) {
        LoginResponseDto response = new LoginResponseDto();
        response.setToken(token);
        response.setRefreshToken(refreshToken);
        response.setId(user.getId());
        response.setUsername(user.getUsername());
        response.setEmail(user.getEmail());
        response.setRole(user.getRole());
        response.setProfileComplete(user.isProfileComplete());  // ← signal to frontend
        return response;
    }

    public User toLoginUserRequest(LoginRequestDto request) {
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(request.getPassword());
        return user;
    }

    // ── User response (admin/internal) ───────────────────────────────────────

    public UserResponseDto mapToUserResponse(User user) {
        UserResponseDto response = new UserResponseDto();
        response.setId(user.getId());
        response.setUsername(user.getUsername());
        response.setFullName(user.getFullName());
        response.setPhoneNumber(user.getPhoneNumber());
        response.setEmail(user.getEmail());
        response.setRole(user.getRole());
        response.setStatus(user.getStatus());

        if (user.getAddress() != null) {
            AddressDto addressDto = new AddressDto();
            addressDto.setStreet(user.getAddress().getStreet());
            addressDto.setCity(user.getAddress().getCity());
            addressDto.setState(user.getAddress().getState());
            addressDto.setCountry(user.getAddress().getCountry());
            addressDto.setZipcode(user.getAddress().getZipcode());
            response.setAddress(addressDto);
        }

        response.setCreatedAt(user.getCreatedAt());
        response.setUpdatedAt(user.getUpdatedAt());
        return response;
    }

    // ── Admin user creation ──────────────────────────────────────────────────

    public User mapToRegisterAdminUserRequest(UserRequestDto request) {
        User user = new User();
        user.setUsername(request.getUsername());
        user.setFullName(request.getFullName());
        user.setEmail(request.getEmail());
        user.setPhoneNumber(request.getPhoneNumber());
        user.setPassword(request.getPassword());
        user.setRole(request.getRole());

        if (request.getAddress() != null) {
            Address address = new Address();
            address.setStreet(request.getAddress().getStreet());
            address.setCity(request.getAddress().getCity());
            address.setState(request.getAddress().getState());
            address.setCountry(request.getAddress().getCountry());
            address.setZipcode(request.getAddress().getZipcode());
            user.setAddress(address);
        }

        user.setStatus(request.getStatus());
        return user;
    }

    // ── Profile completion (user-facing update) ──────────────────────────────

    /**
     * Applies fullName and phoneNumber from the request onto the existing user.
     * Marks profileComplete = true as soon as fullName is provided.
     * Address is intentionally excluded — not needed for a task management app.
     */
    public void mapToCompleteUserRequest(CompleteUserRequestDto request, User user) {
        if (request.getFullName() != null && !request.getFullName().isBlank()) {
            user.setFullName(request.getFullName());
            user.setProfileComplete(true);   // ← mark complete once name is set
        }
        if (request.getPhoneNumber() != null && !request.getPhoneNumber().isBlank()) {
            user.setPhoneNumber(request.getPhoneNumber());
        }
    }

    // ── SMTP mappings ────────────────────────────────────────────────────────

    public VerifyEmailResponseDto verifyEmailResponse(boolean success, String message) {
        VerifyEmailResponseDto response = new VerifyEmailResponseDto();
        response.setVerified(success);
        response.setMessage(message);
        return response;
    }


    public ResendVerificationResponseDto resendVerificationResponse(boolean sent, String message, UUID userId) {
        ResendVerificationResponseDto response = new ResendVerificationResponseDto();
        response.setSent(sent);
        response.setMessage(message);
        response.setUserId(userId);   // ← pass through so frontend gets it
        return response;
    }

    // ── Google OAuth ─────────────────────────────────────────────────────────

    /**
     * Maps User + token data to GoogleAuthResponseDto.
     * profileComplete is true only if the user already has a fullName set
     * (Google provides this for most accounts, but it can be null).
     */
    public GoogleAuthResponseDto mapToGoogleAuthResponse(
            User user,
            String accessToken,
            String refreshToken,
            boolean isNewUser
    ) {
        return GoogleAuthResponseDto.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .userId(user.getId().toString())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .avatarUrl(user.getAvatarUrl())
                .isNewUser(isNewUser)
                .profileComplete(user.isProfileComplete())  // ← signal to frontend
                .build();
    }
}