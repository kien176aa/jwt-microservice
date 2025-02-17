package com.javatechie.controller;

import com.javatechie.dto.AuthRequest;
import com.javatechie.exceptions.PermissionException;
import com.javatechie.service.AuthService;
import lombok.extern.slf4j.Slf4j;
import org.example.dtos.CheckPermissionRequest;
import org.example.dtos.CommonResponse;
import org.example.dtos.UserDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.web.bind.annotation.*;

import java.io.File;

@RestController
@RequestMapping("/auth")
@Slf4j
public class AuthController {
    @Autowired
    private AuthService service;

    @Autowired
    private AuthenticationManager authenticationManager;

    @PostMapping("/register")
    public CommonResponse<?> addNewUser(@RequestBody UserDto user) {
        return CommonResponse.ok(service.saveUser(user));
    }

    @PostMapping("/login")
    public CommonResponse<?> login(@RequestBody AuthRequest authRequest) throws Exception {
//        Authentication authenticate = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(authRequest.getUsername(), authRequest.getPassword()));
//        if (authenticate.isAuthenticated()) {
//            return service.generateToken(authRequest.getUsername());
//        } else {
//            throw new RuntimeException("invalid access");
//        }
        return CommonResponse.ok(service.login(authRequest));
    }

    @PostMapping("/validate")
    public CommonResponse<String> validateToken(@RequestBody CheckPermissionRequest request) {
        log.info("Access: {}", request.getUrl());
        service.validateToken(request);
        return CommonResponse.ok("Token is valid");
    }

    @GetMapping("/test")
    public CommonResponse<?> test(){
        return CommonResponse.ok();
    }
}
