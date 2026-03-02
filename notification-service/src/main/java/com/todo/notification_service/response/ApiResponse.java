package com.todo.notification_service.response;



import com.todo.notification_service.dto.ResponseDto;

import java.time.LocalDateTime;

public class ApiResponse {
    public static ResponseDto buildResponse(Object data, int statusCode, String message){
        ResponseDto responseDto = new ResponseDto();
        responseDto.setData(data);
        responseDto.setStatusCode(statusCode);
        responseDto.setMessage(message);
        responseDto.setTimeRequested(LocalDateTime.now());

        return responseDto;
    }
}
