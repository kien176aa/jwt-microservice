package com.javatechie.controller;

import com.javatechie.service.AuthService;
import lombok.extern.slf4j.Slf4j;
import org.example.dtos.CommonResponse;
import org.example.dtos.UserDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
@Slf4j
public class UserController {

    @Autowired
    private AuthService service;

    @GetMapping("/change-status/{id}")
    public CommonResponse<?> changeStatus(@PathVariable Long id) throws Exception {
        log.info("changeStatus");
        return CommonResponse.ok(service.changeStatus(id));
    }

    @PostMapping("/create")
    public CommonResponse<?> create(@RequestBody UserDto req){
        log.info("Create user {}", req);
        return CommonResponse.ok(service.create(req));
    }
}
