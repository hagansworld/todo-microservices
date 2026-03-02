//package com.todo_service.response;
//
//import com.todo_service.dto.ResponseDto;
//
//import java.time.LocalDateTime;
//
//public class ApiResponse {
//    public static ResponseDto buildResponse(Object data, int statusCode, String message){
//        ResponseDto responseDto = new ResponseDto();
//        responseDto.setData(data);
//        responseDto.setStatusCode(statusCode);
//        responseDto.setMessage(message);
//        responseDto.setTimeRequested(LocalDateTime.now());
//
//        return responseDto;
//    }
//}
package com.todo_service.response;

import com.todo_service.dto.ResponseDto;

import java.time.LocalDateTime;

public class ApiResponse {

    public static <T> ResponseDto<T> buildResponse(
            T data,
            int statusCode,
            String message
    ) {
        ResponseDto<T> responseDto = new ResponseDto<>();
        responseDto.setData(data);
        responseDto.setStatusCode(statusCode);
        responseDto.setMessage(message);
        responseDto.setTimeRequested(LocalDateTime.now());

        return responseDto;
    }
}
