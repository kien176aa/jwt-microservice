package com.javatechie.exception;

import lombok.extern.slf4j.Slf4j;
import org.example.dtos.CommonResponse;
import org.example.exception.InvalidVoucherException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
public class OrderGlobalExceptionHandler {
    @ExceptionHandler(Exception.class)
    public CommonResponse<?> handleException(Exception ex){
        log.error("Order Exception: {}", ex.getMessage());
        return CommonResponse.notOk(ex.getMessage());
    }

    @ExceptionHandler(InvalidVoucherException.class)
    public CommonResponse<?> handlePermissionException(Exception ex){
        log.error("InvalidVoucherException: {}", ex.getMessage());
        return CommonResponse.notOk(ex.getMessage());
    }
}
