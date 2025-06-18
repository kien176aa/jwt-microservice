package com.javatechie.client;

import org.example.dtos.CommonResponse;
import org.example.dtos.UserDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "identity-service", url = "${url.identity.service}")
public interface IdentityClient {

    @GetMapping("/auth/current-user")
    CommonResponse<UserDto> getCurrentUser(@RequestHeader("Authorization") String token);
}

