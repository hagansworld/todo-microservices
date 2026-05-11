package com.todo.user_service.controller;

import com.todo.user_service.dto.*;
import com.todo.user_service.response.ApiResponse;
import com.todo.user_service.service.AuthService;
import com.todo.user_service.service.GoogleOAuthService;
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
    private final GoogleOAuthService googleOAuthService;


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
    public ResponseEntity<ResponseDto> updateUserProfile(@Valid @RequestBody CompleteUserRequestDto completeUserRequestDto, @PathVariable UUID id){

        UserResponseDto response = authService.updateUserProfile(completeUserRequestDto, id);

        // Return the updated user profile response
        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.buildResponse(
                        response,
                        HttpStatus.OK.value(),
                        "Profile updated successfully"
                ));
    }


// ─────────────────────────────────────────────────────────────
// GOOGLE OAUTH ENDPOINTS
// ─────────────────────────────────────────────────────────────

    /**
     * Google Sign-Up
     * Frontend sends the raw id_token returned by Google after the
     * user completes the OAuth consent screen.
     * First-time users are created and sent a welcome email.
     * Returning users are signed in automatically (same endpoint is safe for both).
     *
     * @author Isaac Hagan
     */
    @Operation(
            summary = "Google Sign-Up",
            description = "Register a new user via Google OAuth. Sends a welcome email on first sign-up."
    )
    @PostMapping("/google/signup")
    public ResponseEntity<ResponseDto> googleSignUp(
            @Valid @RequestBody GoogleAuthRequestDto request) {

        GoogleAuthResponseDto response = googleOAuthService.handleGoogleAuth(request.getIdToken());

        int statusCode     = response.isNewUser() ? HttpStatus.CREATED.value() : HttpStatus.OK.value();
        String message     = response.isNewUser()
                ? "Account created successfully via Google"
                : "Signed in successfully via Google";

        return ResponseEntity.status(statusCode)
                .body(ApiResponse.buildResponse(response, statusCode, message));
    }

    /**
     * Google Sign-In
     * Same underlying logic as sign-up — the service layer decides
     * whether this is a new or returning user based on the email.
     * isNewUser in the response tells the frontend what happened.
     *
     * @author Isaac Hagan
     */
    @Operation(
            summary = "Google Sign-In",
            description = "Authenticate an existing user via Google OAuth."
    )
    @PostMapping("/google/signin")
    public ResponseEntity<ResponseDto> googleSignIn(
            @Valid @RequestBody GoogleAuthRequestDto request) {

        GoogleAuthResponseDto response = googleOAuthService.handleGoogleAuth(request.getIdToken());

        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.buildResponse(
                        response,
                        HttpStatus.OK.value(),
                        "Signed in successfully via Google"
                ));
    }



    }
