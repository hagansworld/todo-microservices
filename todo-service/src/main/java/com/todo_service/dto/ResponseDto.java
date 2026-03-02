package com.todo_service.dto;//package com.todo_service.dto;
//
//import com.fasterxml.jackson.annotation.JsonInclude;
//import lombok.AllArgsConstructor;
//import lombok.Builder;
//import lombok.Data;
//import lombok.NoArgsConstructor;
//
//import java.time.LocalDateTime;
//
//@Data
//@AllArgsConstructor
//@NoArgsConstructor
//@Builder
//@JsonInclude(JsonInclude.Include.NON_NULL)
//public class ResponseDto {
//    private Object data;
//    private Integer statusCode;
//    private String message;
//    private LocalDateTime timeRequested;
//}

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ResponseDto<T> {
    private T data;
    private Integer statusCode;
    private String message;
    private LocalDateTime timeRequested;
}
