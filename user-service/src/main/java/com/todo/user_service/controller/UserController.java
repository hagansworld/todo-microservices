package com.todo.user_service.controller;

import com.todo.user_service.dto.ResponseDto;
import com.todo.user_service.dto.UserRequestDto;
import com.todo.user_service.dto.UserResponseDto;
import com.todo.user_service.response.ApiResponse;
import com.todo.user_service.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Tag(name = "User Service", description = "Admin operations for managing users")
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class UserController {
    private  final UserService userService;

    @Operation(summary = "Create a new user", description = "Admin creates a new user account")
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/create-user")
    public ResponseEntity<ResponseDto>createUser(@RequestBody UserRequestDto userRequestDto){
        UserResponseDto response = userService.createUser(userRequestDto);
        return ResponseEntity.status(201)
                .body(ApiResponse.buildResponse(
                        response,
                        HttpStatus.CREATED.value(),
                        "User Created Successfully"
                ));
    }

    @Operation(summary = "Get all users", description = "Retrieve all users (Admin only)")
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public ResponseEntity<ResponseDto>getAllUsers(){
        List<UserResponseDto> response = userService.getAllUsers();
        return  ResponseEntity.ok()
                .body(ApiResponse.buildResponse(
                        response,
                        200,
                        "Users retrieved successfully"
                ));

    }
    @Operation(summary = "Get user by ID", description = "Retrieve a user using their UUID")
    @PreAuthorize("hasAnyRole('ADMIN','SERVICE')")
    @GetMapping("/{id}")
    public ResponseEntity<ResponseDto>getUserById(@PathVariable UUID id){
        UserResponseDto response = userService.getUserById(id);
        return ResponseEntity.ok()
                .body(ApiResponse.buildResponse(
                        response,
                        200,
                        " User retrieved successfully"
                ));
    }

    @Operation(summary = "Update user", description = "Update user details by ID")
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/update-user/{id}")
    public ResponseEntity<ResponseDto>updateUserById(@PathVariable UUID id, @RequestBody UserRequestDto requestDto){
        UserResponseDto response = userService.updateUserById(id,requestDto);
        return ResponseEntity.ok()
                .body(ApiResponse.buildResponse(
                        response,
                        200,
                        "User updated successfully"
                ));
    }

    @Operation(summary = "Delete user", description = "Delete a user by ID")
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/delete-user/{id}")
    public ResponseEntity<ResponseDto> deleteUserById(@PathVariable UUID id){
        UserResponseDto response = userService.deleteUser(id);
        return ResponseEntity.ok()
                .body(ApiResponse.buildResponse(
                        response,
                        200,
                        "User deleted successfully"
                ));
    }

    @Operation(summary = "Search users", description = "Search users by keyword (email, name, username)")
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/search")
    public ResponseEntity<ResponseDto>searchUsers(@RequestParam String Keyword){
        List<UserResponseDto> response = userService.searchUsers(Keyword);
        return ResponseEntity.ok()
                .body(ApiResponse.buildResponse(
                        response,
                        200,
                        "Users matching keyword retrieved successfully"
                ));

    }


    }

