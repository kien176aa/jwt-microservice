package com.javatechie.exceptions;

import lombok.extern.slf4j.Slf4j;
import org.example.dtos.CommonResponse;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
public class IdentityGlobalExceptionHandler {
    @ExceptionHandler(Exception.class)
    public CommonResponse<?> handleException(Exception ex){
        log.error("Iden Exception: {}", ex.getMessage());
        return CommonResponse.notOk(ex.getMessage());
    }

    @ExceptionHandler(PermissionException.class)
    public CommonResponse<?> handlePermissionException(Exception ex){
        log.error("Permission Exception: {}", ex.getMessage());
        return CommonResponse.notOk(ex.getMessage());
    }
}
