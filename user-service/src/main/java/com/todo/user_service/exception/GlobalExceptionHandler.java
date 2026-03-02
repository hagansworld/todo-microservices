package com.todo.user_service.exception;

import com.todo.user_service.dto.ResponseDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.validation.FieldError;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

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

    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<ResponseDto>handleDuplicateResourceException(DuplicateResourceException ex){
        ResponseDto errorResponse = new ResponseDto();
        errorResponse.setStatusCode(HttpStatus.CONFLICT.value());
        errorResponse.setMessage(ex.getMessage());
        errorResponse.setTimeRequested(LocalDateTime.now());

        return new ResponseEntity<>(errorResponse,HttpStatus.CONFLICT);
    }



    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<ResponseDto> handleInvalidCredentialsException(InvalidCredentialsException ex){
        ResponseDto errorResponse = ResponseDto.builder()
                .statusCode(HttpStatus.UNAUTHORIZED.value()) // 401
                .message(ex.getMessage())
                .timeRequested(LocalDateTime.now())
                .build();

        return new ResponseEntity<>(errorResponse, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(EmailSendFailedException.class)
    public ResponseEntity<ResponseDto> handleEmailSendFailedException(EmailSendFailedException ex){
        ResponseDto errorResponse = ResponseDto.builder()
                .statusCode(HttpStatus.BAD_REQUEST.value()) //400
                .message(ex.getMessage())
                .timeRequested(LocalDateTime.now())
                .build();

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(VerificationCodeExpiredException.class)
    public ResponseEntity<ResponseDto>handleVerificationCodeExpiredException(VerificationCodeExpiredException ex){
        ResponseDto errorResponse = ResponseDto.builder()
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .message(ex.getMessage())
                .timeRequested(LocalDateTime.now())
                .build();

        return new ResponseEntity<>(errorResponse,HttpStatus.BAD_REQUEST);
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

    @ExceptionHandler(AccountAlreadyVerifiedException.class)
    public ResponseEntity<ResponseDto>handleAccountAlreadyVerifiedException(AccountAlreadyVerifiedException ex){
        ResponseDto errorResponse = ResponseDto.builder()
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .message(ex.getMessage())
                .timeRequested(LocalDateTime.now())
                .build();

        return new ResponseEntity<>(errorResponse,HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ResponseDto> handleValidationExceptions(MethodArgumentNotValidException ex) {
        // Collect all validation errors for fields
        List<String> errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.toList());

        // Join all error messages into one string
        String errorMessage = String.join("; ", errors);

        ResponseDto errorResponse = ResponseDto.builder()
                .statusCode(HttpStatus.BAD_REQUEST.value()) // 400
                .message(errorMessage) // Combined validation errors
                .timeRequested(LocalDateTime.now())
                .build();

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }




}
