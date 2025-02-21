package com.javatechie.controller;

import com.javatechie.dto.AuthRequest;
import com.javatechie.service.AuthService;
import com.javatechie.service.JwtService;
import lombok.extern.slf4j.Slf4j;
import org.example.constants.ErrorMessage;
import org.example.dtos.CheckPermissionRequest;
import org.example.dtos.CommonResponse;
import org.example.dtos.UserDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.io.File;

@RestController
@RequestMapping("/auth")
@Slf4j
public class AuthController {
    @Autowired
    private AuthService service;
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    @Autowired
    private JwtService jwtService;

    @PostMapping("/register")
    public CommonResponse<?> addNewUser(@RequestBody UserDto user) {
        return CommonResponse.ok(service.saveUser(user));
    }

    @PostMapping("/login")
    public CommonResponse<?> login(@RequestBody AuthRequest authRequest) throws Exception {
        return CommonResponse.ok(service.login(authRequest));
    }

    @PostMapping("/validate")
    public CommonResponse<String> validateToken(@RequestBody CheckPermissionRequest request) {
        log.info("Access: {}", request.getUrl());
        service.validateToken(request);
        return CommonResponse.ok("Token is valid");
    }

    @GetMapping("/current-user")
    public CommonResponse<?> getCurrentUser(@RequestHeader("Authorization") String token) {
        log.info("getCurrentUser 1: {}", token);
        jwtService.validateToken(token);
        token = token.substring(7);
        Object user = redisTemplate.opsForValue().get("USER_" + token);
        log.info("getCurrentUser 2: {}", user);
        if (user == null) {
            return CommonResponse.unAuth(ErrorMessage.UN_AUTH2);
        }
        return CommonResponse.ok(user);
    }

    @GetMapping("/test")
    public CommonResponse<?> test(){
        return CommonResponse.ok();
    }
}
