package com.blog_application.blogApp.exceptionHandler;

import com.blog_application.blogApp.payloads.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ApiResponse> userNotFoundException(UserNotFoundException ex)
    {
        ApiResponse apiResponse = new ApiResponse(ex.getMessage(),false);
        return new ResponseEntity<ApiResponse>(apiResponse,HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(CategoryNotFoundException.class)
    public ResponseEntity<ApiResponse> categoryNotFoundExceptionHandler(CategoryNotFoundException ex)
    {
        ApiResponse apiResponse = new ApiResponse(ex.getMessage(),false);
        return new ResponseEntity<>(apiResponse,HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>>methodArgumentNotValidExceptionHandler(MethodArgumentNotValidException ex)
    {
        Map<String,String> errorResponse = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String message = error.getDefaultMessage();
            errorResponse.put(fieldName,message);
        });
        return new ResponseEntity<>(errorResponse,HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiResponse> httpRequestMethodNotSupportedExceptionHandler(HttpRequestMethodNotSupportedException ex)
    {
        ApiResponse apiResponse = new ApiResponse(ex.getMessage(),false);
        return new ResponseEntity<ApiResponse>(apiResponse,HttpStatus.BAD_REQUEST);
    }


}
