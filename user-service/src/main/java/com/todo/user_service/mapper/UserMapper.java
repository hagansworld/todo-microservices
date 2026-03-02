package com.todo.user_service.mapper;

import com.todo.user_service.dto.*;
import com.todo.user_service.entity.Address;
import com.todo.user_service.entity.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    // map User to RegisterResponseDto
    public RegisterResponseDto mapToRegisterUserResponse(User user, boolean emailSent){
        RegisterResponseDto response = new RegisterResponseDto();
        response.setId(user.getId());
        response.setUsername(user.getUsername());
        response.setEmail(user.getEmail());
        response.setEmailSent(emailSent);
        response.setMessage(emailSent ? "Verification email sent" : "failed to send email");

        return  response;
    }

    // map RegisterRequestDto to User
    public User toRegisterUserRequest(RegisterRequestDto request){
            User user= new User();
            user.setUsername(request.getUsername());
            user.setEmail(request.getEmail());
            user.setPassword(request.getPassword());
            return user;
    }

    // map User to LoginResponse
    public LoginResponseDto toLoginUserResponse(User user, String token, String refreshToken){
        LoginResponseDto response = new LoginResponseDto();
        response.setToken(token);
        response.setRefreshToken(refreshToken);
        response.setId(user.getId());
        response.setUsername(user.getUsername());
        response.setEmail(user.getEmail());
        response.setRole(user.getRole());

        return  response;
    }

    // map LoginRequest to User
    public User toLoginUserRequest(LoginRequestDto request){
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(request.getPassword());
        return  user;
    }


    // map User to UserResponse
    public UserResponseDto mapToUserResponse(User user){
        UserResponseDto response = new UserResponseDto();

        // Set the properties in the desired order
        response.setId(user.getId());
        response.setUsername(user.getUsername());
        response.setFullName(user.getFullName());
        response.setPhoneNumber(user.getPhoneNumber());
        response.setEmail(user.getEmail());
        response.setRole(user.getRole());
        response.setStatus(user.getStatus());

        // Set the address last
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

    // map adminUserRequest to User :::ADMIN ONLY
    public User mapToRegisterAdminUserRequest(UserRequestDto request){
        User user = new User();
        user.setUsername(request.getUsername());
        user.setFullName(request.getFullName());
        user.setEmail(request.getEmail());
        user.setPhoneNumber(request.getPhoneNumber());
        user.setPassword(request.getPassword());
        user.setRole(request.getRole());

       if (request.getAddress() !=null){
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

    // Map CompleteUserRequestDto to User entity
    public void mapToCompleteUserRequest(CompleteUserRequestDto request,User user){
        user.setFullName(request.getFullName());
        user.setPhoneNumber(request.getPhoneNumber());
        if (request.getAddress() != null) {
            Address address = new Address();
            address.setStreet(request.getAddress().getStreet());
            address.setCity(request.getAddress().getCity());
            address.setState(request.getAddress().getState());
            address.setCountry(request.getAddress().getCountry());
            address.setZipcode(request.getAddress().getZipcode());

            user.setAddress(address);
        }

    }


    /* ***************
    * SMTP MAPPINGS
    * ****************/

    // Map VerifyEmail Response
    public  VerifyEmailResponseDto verifyEmailResponse(boolean success, String message){
        VerifyEmailResponseDto response = new VerifyEmailResponseDto();
        response.setVerified(success);
        response.setMessage(message);

        return  response;
    }

    // map ResendVerification Response
    public ResendVerificationResponseDto resendVerificationResponse(boolean sent, String message){
        ResendVerificationResponseDto response = new ResendVerificationResponseDto();
        response.setSent(sent);
        response.setMessage(message);

        return  response;
    }



}
