package com.todo_service.exception;

import com.todo_service.dto.ResponseDto;
import io.jsonwebtoken.ExpiredJwtException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.LocalDateTime;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ResponseDto>handleNotFoundException(NotFoundException ex){
        ResponseDto errorResponse = new ResponseDto();
        errorResponse.setStatusCode(HttpStatus.NOT_FOUND.value());
        errorResponse.setMessage(ex.getMessage());
        errorResponse.setTimeRequested(LocalDateTime.now());
        return new ResponseEntity<>(errorResponse,HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(NotificationServiceException.class)
    public ResponseEntity<ResponseDto> handleNotificationServiceException(NotificationServiceException ex){
        ResponseDto errorResponse = ResponseDto.builder()
                .statusCode(HttpStatus.BAD_REQUEST.value()) //400
                .message(ex.getMessage())
                .timeRequested(LocalDateTime.now())
                .build();

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }


    @ExceptionHandler(UserServiceException.class)
    public ResponseEntity<ResponseDto> handleUserServiceException(UserServiceException ex){
        ResponseDto errorResponse = ResponseDto.builder()
                .statusCode(HttpStatus.BAD_REQUEST.value()) //400
                .message(ex.getMessage())
                .timeRequested(LocalDateTime.now())
                .build();

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }


    @ExceptionHandler(InactiveUserException.class)
    public ResponseEntity<ResponseDto> handleInactiveUserExceptionException(InactiveUserException ex){
        ResponseDto errorResponse = ResponseDto.builder()
                .statusCode(HttpStatus.BAD_REQUEST.value()) //400
                .message(ex.getMessage())
                .timeRequested(LocalDateTime.now())
                .build();

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }


    @ExceptionHandler(ExpiredJwtException.class)
    public ResponseEntity<String> handleExpiredJwtException(ExpiredJwtException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body("Token expired. Please login again.");
    }

}
