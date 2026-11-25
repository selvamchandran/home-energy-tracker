package com.selvam.userservice.exceptions;



import com.selvam.userservice.dto.ApiResponseDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponseDto> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();

        ex.getBindingResult().getFieldErrors().forEach(error ->
                errors.put(error.getField(), error.getDefaultMessage())
        );

        return ResponseEntity.badRequest()
                .body(new ApiResponseDto("Validation failed", errors, HttpStatus.BAD_REQUEST));
    }


    // Handle ResourceNotFoundException
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponseDto> handleResourceNotFound(ResourceNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ApiResponseDto(ex.getMessage(), null, HttpStatus.NOT_FOUND));
    }

    @ExceptionHandler(InvalidFieldException.class)
    public ResponseEntity<ApiResponseDto> handleInvalidFieldException(InvalidFieldException ex){
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ApiResponseDto(ex.getMessage(),null,HttpStatus.BAD_REQUEST));
    }

    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<ApiResponseDto> handleDuplicateException(DuplicateResourceException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ApiResponseDto(ex.getMessage(), null, HttpStatus.CONFLICT));
    }

   /* @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponseDto> handleAccessDenied(AccessDeniedException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(new ApiResponseDto("Access denied: " + ex.getMessage(), null, HttpStatus.FORBIDDEN));
    }*/
    // Handle any other unhandled exceptions
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponseDto> handleGenericException(Exception ex) {
//        ex.printStackTrace(); // 👈 this will print full stacktrace in console
        log.error("Unexpected error occurred", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponseDto("An unexpected error occurred", null, HttpStatus.INTERNAL_SERVER_ERROR));
    }
}
