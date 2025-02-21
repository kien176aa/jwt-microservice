package com.javatechie.exception;

import org.example.dtos.CommonResponse;
import org.example.exception.NotFoundException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(NotFoundException.class)
    public CommonResponse<?> handleOrderNotFoundException(NotFoundException ex){
        return CommonResponse.notFound(ex.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public CommonResponse<?> handleException(Exception ex){
        return CommonResponse.notOk(ex.getMessage());
    }
}
