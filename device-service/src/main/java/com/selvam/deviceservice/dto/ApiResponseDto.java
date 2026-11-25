package com.selvam.deviceservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class ApiResponseDto<T> {
   /* private String message;
    private Object data;
    private int statusCode;
    private LocalDateTime timestamp;*/

//    @Schema(description = "Response message", example = "Task created successfully")
    private String message;

//    @Schema(description = "Response payload", oneOf = { TaskResponse.class, CategoryResponse.class })
    private T data;

//    @Schema(description = "HTTP status code", example = "201")
    private int statusCode;

//    @Schema(description = "Response timestamp")
    private LocalDateTime timestamp;

    public ApiResponseDto(String message, T data) {
        this.message = message;
        this.data = data;
        this.statusCode = HttpStatus.OK.value();
        this.timestamp = LocalDateTime.now();
    }

    public ApiResponseDto(String message, T data, HttpStatus status) {
        this.message = message;
        this.data = data;
        this.statusCode = status.value();
        this.timestamp = LocalDateTime.now();
    }
}
