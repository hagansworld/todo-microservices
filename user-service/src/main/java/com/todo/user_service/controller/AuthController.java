package com.todo.user_service.controller;

import com.todo.user_service.dto.*;
import com.todo.user_service.response.ApiResponse;
import com.todo.user_service.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Tag(name = "Authentication Service", description = "Authentication and account management APIs")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;

    @Operation(summary = "Register user", description = "Create a new user and send verification email")
    @PostMapping("/register-user")
    public ResponseEntity<ResponseDto> registerUser(@Valid @RequestBody RegisterRequestDto registerRequestDto){
        RegisterResponseDto response = authService.registerUser(registerRequestDto);
        return ResponseEntity.status(201)
                .body(ApiResponse.buildResponse(
                        response,
                        HttpStatus.CREATED.value(),
                        "User Created Successfully"
                ));
    }

    @Operation(summary = "Login user", description = "Authenticate user and return access token")
    @PostMapping("/login-user")
    public ResponseEntity<ResponseDto>loginUser(@Valid @RequestBody LoginRequestDto loginRequestDto){
        LoginResponseDto response = authService.loginUser(loginRequestDto);
        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.buildResponse(
                        response,
                        HttpStatus.OK.value(),
                        "Login Successfully"
                ));
    }

    @Operation(summary = "Verify email", description = "Verify user account using verification code")
    @PostMapping("/verify-user")
    public ResponseEntity<ResponseDto> verifyUser(@Valid @RequestBody VerifyEmailRequestDto verifyEmailRequestDto) {
        VerifyEmailResponseDto response = authService.verifyUser(verifyEmailRequestDto);
        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.buildResponse(
                        response,
                        HttpStatus.CREATED.value(),
                        "verification sent successful"
                ));
    }

    @Operation(summary = "Resend verification code", description = "Resend email verification code")
    @PostMapping("/resend-verification")
    public ResponseEntity<ResponseDto> resendVerificationCode(@Valid @RequestBody ResendVerificationRequestDto requestDto) {
        ResendVerificationResponseDto response = authService.resendVerificationCode(requestDto);

        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.buildResponse(
                        response,
                        HttpStatus.OK.value(),
                        "verification sent successful"

                ));
    }

    @Operation(summary = "Request password reset", description = "Send password reset link to email")
    @PostMapping("/request-password-reset")
    public ResponseEntity<ResponseDto> requestPasswordReset(@Valid @RequestBody PasswordResetRequestDto request) {
        PasswordResetResponseDto response = authService.requestPasswordReset(request);
        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.buildResponse(
                        response,
                        HttpStatus.OK.value(),
                        "Password reset link sent successfully"
                ));
    }

    @Operation(summary = "Reset password", description = "Reset password using token")
    @PostMapping("/reset-password")
    public ResponseEntity<ResponseDto> resetPassword(@Valid @RequestBody ResetPasswordDto request) {
        PasswordResetResponseDto response = authService.resetPassword(request);
        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.buildResponse(
                        response,
                        HttpStatus.OK.value(),
                        "Password reset successful"
                ));
    }


    //    @PreAuthorize("hasRole('PATIENT') or hasRole('ADMIN')")
    @Operation(summary = "Update user profile", description = "Updates the user profile after verification.")
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping("/update-profile/{id}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ResponseDto> updateUserProfile(@RequestBody CompleteUserRequestDto completeUserRequestDto, @PathVariable UUID id){

        UserResponseDto response = authService.updateUserProfile(completeUserRequestDto, id);

        // Return the updated user profile response
        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.buildResponse(
                        response,
                        HttpStatus.OK.value(),
                        "Profile updated successfully"
                ));
    }


    }
